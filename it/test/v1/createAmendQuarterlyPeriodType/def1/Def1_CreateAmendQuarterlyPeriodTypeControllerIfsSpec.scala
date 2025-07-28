/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.createAmendQuarterlyPeriodType.def1

import api.models.errors._
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class Def1_CreateAmendQuarterlyPeriodTypeControllerIfsSpec extends IntegrationBaseSpec {

  override def servicesConfig: Map[String, Any] =
    Map("feature-switch.ifs_hip_migration_2089.enabled" -> false) ++ super.servicesConfig

  private val requestBodyJson = Json.parse("""
      |{
      | "quarterlyPeriodType": "standard"
      |}
      |""".stripMargin)

  private val downstreamRequestBodyJson = Json.parse("""
      |{
      | "QRT": "Standard"
      |}
      |""".stripMargin)

  private def errorBody(code: String): String =
    s"""
       |      {
       |        "code": "$code",
       |        "reason": "message"
       |      }
  """.stripMargin

  "Calling the create amend quarterly period type endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()

          DownstreamStub
            .when(DownstreamStub.PUT, downstreamUri)
            .withRequestBody(downstreamRequestBodyJson)
            .thenReturn(OK, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe NO_CONTENT
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
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

            val response: WSResponse = await(request().put(requestBody))
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }

        val input = List(
          ("AA1123A", "XAIS12345678910", "2022-23", requestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "invalid", "2022-23", requestBodyJson, BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "invalid", requestBodyJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2022-23", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError)
        )

        input.foreach((validationErrorTest _).tupled)
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

            val response: WSResponse = await(request().put(requestBodyJson))
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_INCOME_SOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, RuleBusinessIdNotFoundError),
          (CONFLICT, "INCOME_SOURCE_STATE_CONFLICT", BAD_REQUEST, RuleBusinessIdStateConflictError),
          (UNPROCESSABLE_ENTITY, "INVALID_PATH_PARAMETERS", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "WRONG_TAX_YEAR_PROVIDED", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "REQUIRED_PARAMETER_MISSING", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "INVALID_INCOME_SOURCE_TYPE", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_SUBMISSION", BAD_REQUEST, RuleBusinessIdStateConflictError),
          (UNPROCESSABLE_ENTITY, "ANNUAL_INCOME_SOURCE", BAD_REQUEST, RuleBusinessIdStateConflictError),
          (UNPROCESSABLE_ENTITY, "QUARTER_REPORTING_UPDATING_ERROR", BAD_REQUEST, RuleQuarterlyPeriodUpdatingError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        errors.foreach((serviceErrorTest _).tupled)
      }
    }
  }

  private trait Test {
    val nino: String       = "AA123456A"
    val businessId: String = "X0IS12345678901"
    val taxYear: String    = "2023-24"

    def setupStubs(): StubMapping

    private def mtdUri: String = s"/$nino/$businessId/$taxYear"

    def request(): WSRequest = {
      AuthStub.resetAll()
      setupStubs()

      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def downstreamUri: String = s"/income-tax/23-24/income-sources/reporting-type/$nino/$businessId"

  }

}
