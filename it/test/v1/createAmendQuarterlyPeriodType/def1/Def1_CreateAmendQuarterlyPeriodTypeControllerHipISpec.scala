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

package v1.createAmendQuarterlyPeriodType.def1

import api.models.errors.*
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.*
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class Def1_CreateAmendQuarterlyPeriodTypeControllerHipISpec extends IntegrationBaseSpec {

  private val requestBodyJson = Json.parse("""
      |{
      | "quarterlyPeriodType": "standard"
      |}
      |""".stripMargin)

  private val downstreamRequestBodyJson = Json.parse("""
      |{
      | "quarterReportingType": "STANDARD"
      |}
      |""".stripMargin)

  private def errorBody(code: String): String =
    s"""
       |{
       |  "response":
       |    {
       |      "errorCode": "$code",
       |      "errorDescription": "some reason"
       |    }
       |
       |}
  """.stripMargin

  "Calling the create amend quarterly period type endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()

          DownstreamStub
            .when(DownstreamStub.PUT, downstreamUri, downstreamQueryParams)
            .withRequestBody(downstreamRequestBodyJson)
            .thenReturn(NO_CONTENT, JsObject.empty)
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

        input.foreach(validationErrorTest.tupled)
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }

        val errors = List(
          (BAD_REQUEST, "1000", BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          (BAD_REQUEST, "1007", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (CONFLICT, "1121", BAD_REQUEST, RuleBusinessIdStateConflictError),
          (UNPROCESSABLE_ENTITY, "1122", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "1123", BAD_REQUEST, RuleBusinessIdStateConflictError),
          (UNPROCESSABLE_ENTITY, "1124", BAD_REQUEST, RuleBusinessIdStateConflictError),
          (UNPROCESSABLE_ENTITY, "1125", BAD_REQUEST, RuleQuarterlyPeriodUpdatingError),
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "5010", NOT_FOUND, RuleBusinessIdNotFoundError)
        )

        errors.foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {
    val nino: String                               = "AA123456A"
    val businessId: String                         = "X0IS12345678901"
    val taxYear: String                            = "2023-24"
    val downstreamQueryParams: Map[String, String] = Map("taxYear" -> "23-24")

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

    def downstreamUri: String = s"/itsd/income-sources/reporting-type/$nino/$businessId"

  }

}
