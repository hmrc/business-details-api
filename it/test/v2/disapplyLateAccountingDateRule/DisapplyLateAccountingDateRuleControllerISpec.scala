/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v2.disapplyLateAccountingDateRule

import api.models.domain.TaxYear
import api.models.errors._
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class DisapplyLateAccountingDateRuleControllerISpec extends IntegrationBaseSpec {

  private def errorBody(code: String): String =
    s"""
       |{
       |  "response": [
       |    {
       |      "errorCode": "$code",
       |      "errorDescription": "message"
       |    }
       |  ]
       |}
      """.stripMargin

  "Calling the disapply late accounting date rule endpoint" should {
    "return a 204 status code" when {
      "a valid request is made with a past tax year and suspendTemporalValidations is false" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()

          DownstreamStub.onSuccess(
            method = DownstreamStub.PUT,
            uri = downstreamUri,
            queryParams = Map("taxYear" -> "24-25"),
            status = NO_CONTENT
          )
        }

        val response: WSResponse = await(request().post(JsObject.empty))
        response.status shouldBe NO_CONTENT
      }

      "a valid request is made with the current tax year and suspendTemporalValidations is true" in new Test {
        override val taxYear: String                    = TaxYear.currentTaxYear.asMtd
        override val suspendTemporalValidations: String = "true"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)

          DownstreamStub.onSuccess(
            method = DownstreamStub.PUT,
            uri = downstreamUri,
            queryParams = Map("taxYear" -> TaxYear.currentTaxYear.asTysDownstream),
            status = NO_CONTENT
          )
        }

        val response: WSResponse = await(request().post(JsObject.empty))
        response.status shouldBe NO_CONTENT
      }
    }

    "return error according to spec" when {
      "validation error" when {
        "validation error" when {
          def validationErrorTest(requestNino: String,
                                  requestBusinessId: String,
                                  requestTaxYear: String,
                                  expectedStatus: Int,
                                  expectedBody: MtdError): Unit = {
            s"validation fails with ${expectedBody.code} error" in new Test {

              override val nino: String       = requestNino
              override val businessId: String = requestBusinessId
              override val taxYear: String    = requestTaxYear

              override def setupStubs(): StubMapping = {
                AuditStub.audit()
                MtdIdLookupStub.ninoFound(nino)
                AuthStub.authorised()
              }

              val response: WSResponse = await(request().post(JsObject.empty))
              response.status shouldBe expectedStatus
            }
          }

          val input = List(
            ("AA1123A", "XAIS12345678910", "2024-25", BAD_REQUEST, NinoFormatError),
            ("AA123456A", "invalid", "2024-25", BAD_REQUEST, BusinessIdFormatError),
            ("AA123456A", "XAIS12345678910", "invalid", BAD_REQUEST, TaxYearFormatError),
            ("AA123456A", "XAIS12345678910", TaxYear.currentTaxYear().asMtd, BAD_REQUEST, RuleTaxYearNotEndedError),
            ("AA123456A", "XAIS12345678910", "2024-26", BAD_REQUEST, RuleTaxYearRangeInvalidError),
            ("AA123456A", "XAIS12345678910", "2023-24", BAD_REQUEST, RuleTaxYearNotSupportedError)
          )

          input.foreach((validationErrorTest _).tupled)
        }
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().post(JsObject.empty))
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }

        val errors = List(
          (BAD_REQUEST, "1007", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "5009", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "5010", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "1115", BAD_REQUEST, RuleTaxYearNotEndedError),
          (UNPROCESSABLE_ENTITY, "1133", UNPROCESSABLE_ENTITY, RuleNoAccountingDateFoundError),
          (UNPROCESSABLE_ENTITY, "1134", UNPROCESSABLE_ENTITY, RuleElectionPeriodNotExpiredError),
          (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (UNPROCESSABLE_ENTITY, "1242", UNPROCESSABLE_ENTITY, RuleTypeOfBusinessIncorrectError),
          (NOT_IMPLEMENTED, "5000", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        errors.foreach((serviceErrorTest _).tupled)
      }
    }
  }

  private trait Test {
    val nino: String       = "AA123456A"
    val businessId: String = "X0IS12345678901"
    val taxYear: String    = "2024-25"

    val suspendTemporalValidations: String = "false"

    def setupStubs(): StubMapping

    private def mtdUri: String = s"/$nino/$businessId/$taxYear/late-accounting-date-rule-election/disapply"

    def request(): WSRequest = {
      AuthStub.resetAll()
      setupStubs()

      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123"),
          ("suspend-temporal-validations", suspendTemporalValidations)
        )
    }

    def downstreamUri: String = s"/itsd/income-sources/$nino/late-accounting-date/$businessId"
  }

}
