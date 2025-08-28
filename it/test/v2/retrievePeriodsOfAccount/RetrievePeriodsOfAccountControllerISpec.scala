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

package v2.retrievePeriodsOfAccount

import api.models.errors._
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json._
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers._
import support.IntegrationBaseSpec

class RetrievePeriodsOfAccountControllerISpec extends IntegrationBaseSpec {

  "Calling the retrieve periods of account endpoint" should {

    "return a 200 status code" when {
      "any valid request is made and a single business data is returned" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, fullDownstreamJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe fullMtdJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
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

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA1123A", "XAIS12345678901", "2025-26", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "X2", "2025-26", BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678901", "2025-2026", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678901", "2023-24", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "XAIS12345678901", "2025-27", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "1007", BAD_REQUEST, BusinessIdFormatError),
          (UNAUTHORIZED, "5009", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError)
        )

        errors.foreach((serviceErrorTest _).tupled)
      }
    }
  }

  private trait Test {

    val nino: String                               = "AA123456A"
    val businessId: String                         = "XAIS12345678901"
    val taxYear: String                            = "2025-26"
    val downstreamTaxYear: String                  = "25-26"
    val downstreamQueryParams: Map[String, String] = Map("taxYear" -> downstreamTaxYear)

    private def uri: String   = s"/$nino/$businessId/$taxYear/periods-of-account"
    def downstreamUri: String = s"/itsd/income-sources/$nino/periods-of-account/$businessId"

    val fullDownstreamJson: JsValue = Json.parse(
      """
        |{
        |  "submittedOn": "2019-08-24T14:15:22Z",
        |  "periodsOfAccountDates": [
        |    {
        |      "startDate": "2024-04-06",
        |      "endDate": "2025-03-05"
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val fullMtdJson: JsValue = Json.parse(
      s"""
        |{
        |  "submittedOn": "2019-08-24T14:15:22.000Z",
        |  "periodsOfAccount": true,
        |  "periodsOfAccountDates": [
        |    {
        |      "startDate": "2024-04-06",
        |      "endDate": "2025-03-05"
        |    }
        |  ]
        |}
      """.stripMargin
    )

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
        |{
        |  "errors": {
        |    "processingDate": "2024-07-15T09:45:17Z",
        |    "code": "$code",
        |    "text": "error text"
        |  }
        |}
      """.stripMargin

  }

}
