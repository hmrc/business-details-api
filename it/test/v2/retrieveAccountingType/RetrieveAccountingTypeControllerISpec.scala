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

package v2.retrieveAccountingType

import api.models.errors.*
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class RetrieveAccountingTypeControllerISpec extends IntegrationBaseSpec {

  "Calling the retrieve accounting type endpoint" should {

    "return a 200 status code" when {
      "any valid request is made and a success response body is returned" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(
            method = DownstreamStub.GET,
            uri = downstreamUri,
            queryParams = Map("incomeSourceId" -> businessId, "taxYearExplicit" -> "2024-25"),
            status = OK,
            body = downstreamResponseBodyAccountingData
          )
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.body shouldBe responseBodyAccountingDetailsData.toString()
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
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }

        val input = List(
          ("AA1123A", "XAIS12345678910", "2024-25", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "invalid", "2024-25", BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "invalid", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2024-26", BAD_REQUEST, RuleTaxYearRangeInvalidError)
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
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().get())
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }

        val errors = List(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1007", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "1122", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "1229", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "5009", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError)
        )

        errors.foreach((serviceErrorTest _).tupled)
      }
    }
  }

  private trait Test {

    val nino: String       = "AA123456A"
    val businessId: String = "X0IS12345678901"
    val taxYear: String    = "2024-25"

    val responseBodyAccountingDetailsData: JsValue = Json.parse(
      """
        |{
        |  "accountingType": "CASH"
        |}
      """.stripMargin
    )

    val downstreamResponseBodyAccountingData: JsValue = Json.parse(
      """
        |{
        |  "ukProperty": [
        |    {
        |      "incomeSourceType": "02",
        |      "incomeSourceId": "AT0000000000001",
        |      "incomeSourceName": "string",
        |      "cessationDate": "2019-08-24",
        |      "commencementDate": "2019-08-24",
        |      "latency": {},
        |      "accountingPeriodStartDate": "2019-08-24",
        |      "accountingPeriodEndDate": "2019-08-24",
        |      "accountingType": "CASH",
        |      "quarterReporting": {},
        |      "basisPeriodStartDate": "2019-08-24",
        |      "basisPeriodEndDate": "2019-08-24",
        |      "obligations": []
        |    }
        |  ],
        |  "charitableGiving": {
        |    "incomeSourceId": "AT0000000000001",
        |    "endDate": "2019-08-24",
        |    "startDate": "2019-08-24"
        |  },
        |  "bbsi": [
        |    {}
        |  ],
        |  "dividends": {
        |    "incomeSourceId": "AT0000000000001",
        |    "startDate": "2019-08-24",
        |    "endDate": "2019-08-24"
        |  },
        |  "stateBenefits": [
        |    {}
        |  ],
        |  "employments": [
        |    {}
        |  ],
        |  "cgtPpdSubmissions": [
        |    {}
        |  ]
        |}
      """.stripMargin
    )

    def setupStubs(): StubMapping

    private def mtdUri: String = s"/$nino/$businessId/$taxYear/accounting-type"

    def downstreamUri: String = s"/itsd/income-sources/v2/$nino"

    def request(): WSRequest = {
      AuthStub.resetAll()
      setupStubs()

      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
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

  }

}
