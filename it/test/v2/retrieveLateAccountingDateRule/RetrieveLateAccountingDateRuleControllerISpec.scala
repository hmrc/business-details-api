/*
 * Copyright 2026 HM Revenue & Customs
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

package v2.retrieveLateAccountingDateRule

import api.models.errors.*
import api.services.{AuthStub, DownstreamStub, MtdIdLookupStub}
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import support.IntegrationBaseSpec
import v2.retrieveLateAccountingDateRule.fixture.RetrieveLateAccountingDateFixture.{downstreamResponseJson, mtdResponseJson}

class RetrieveLateAccountingDateRuleControllerISpec extends IntegrationBaseSpec {

  "Calling the 'Retrieve Late Accounting Date Rule Election' endpoint" should {
    "return a 200 status code" when {
      "a valid request is made" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DownstreamStub.GET,
          uri = downstreamUri,
          queryParams = downstreamQueryParams,
          status = OK,
          body = downstreamResponseJson
        )

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseJson
        response.header("X-CorrelationId").nonEmpty shouldBe true
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

            val response: WSResponse = await(request().get())
            response.json shouldBe expectedBody.asJson
            response.status shouldBe expectedStatus
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input: Seq[(String, String, String, Int, MtdError)] = List(
          ("AA1123A", "XAIS12345678910", "2025-26", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "invalid", "2025-26", BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "invalid", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2025-27", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )

        input.foreach(validationErrorTest.tupled)
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {
            override def setupStubs(): Unit = DownstreamStub.onError(
              method = DownstreamStub.GET,
              uri = downstreamUri,
              queryParams = downstreamQueryParams,
              errorStatus = downstreamStatus,
              errorBody = errorBody(downstreamCode)
            )

            val response: WSResponse = await(request().get())
            response.json shouldBe expectedBody.asJson
            response.status shouldBe expectedStatus
            response.header("X-CorrelationId").nonEmpty shouldBe true
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val errors = List(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1007", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "1122", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "1229", INTERNAL_SERVER_ERROR, InternalError),
          (UNAUTHORIZED, "5009", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError)
        )

        errors.foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {

    val nino: String       = "AA123456A"
    val businessId: String = "X0IS12345678901"
    val taxYear: String    = "2025-26"

    def setupStubs(): Unit = ()

    private def mtdUri: String = s"/$nino/$businessId/$taxYear/late-accounting-date-rule-election"

    def downstreamUri: String = s"/itsd/income-sources/v2/$nino"

    val downstreamQueryParams: Map[String, String] = Map("incomeSourceId" -> businessId, "taxYearExplicit" -> taxYear)

    def request(): WSRequest = {
      AuthStub.resetAll()
      setupStubs()
      MtdIdLookupStub.ninoFound(nino)
      AuthStub.authorised()
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
