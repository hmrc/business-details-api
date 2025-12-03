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

package v2.createUpdatePeriodsOfAccount

import api.models.errors.*
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import api.utils.JsonErrorValidators
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.*
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import support.IntegrationBaseSpec
import v2.fixtures.CreateUpdatePeriodsOfAccountFixtures.validFullRequestBodyJson

class CreateUpdatePeriodsOfAccountControllerISpec extends IntegrationBaseSpec with JsonErrorValidators {

  "Calling the create or update periods of account endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub
            .when(DownstreamStub.PUT, downstreamUri, downstreamQueryParam)
            .withRequestBody(downstreamRequestBodyJson)
            .thenReturn(NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(validFullRequestBodyJson))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
      }
    }

    "return error according to spec" when {

      val invalidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "periodsOfAccount": false,
          |  "periodsOfAccountDates": [
          |    {
          |      "startDate": "2025",
          |      "endDate": "2025"
          |    },
          |    {
          |      "startDate": "2025-07-06",
          |      "endDate": "2025-07-05"
          |    },
          |    {
          |      "startDate": "2026-04-06",
          |      "endDate": "2026-07-05"
          |    }
          |  ]
          |}
        """.stripMargin
      )

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                errorWrapper: Option[ErrorWrapper]): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String       = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String    = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val expectedBodyJson: JsValue = errorWrapper match {
              case Some(wrapper) => Json.toJson(wrapper)
              case None          => Json.toJson(expectedBody)
            }

            val response: WSResponse = await(request().put(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBodyJson)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "X0IS12345678901", "2025-26", validFullRequestBodyJson, BAD_REQUEST, NinoFormatError, None),
          ("AA123456A", "X0IS12345678901", "2025", validFullRequestBodyJson, BAD_REQUEST, TaxYearFormatError, None),
          ("AA123456A", "X0IS123", "2025-26", validFullRequestBodyJson, BAD_REQUEST, BusinessIdFormatError, None),
          ("AA123456A", "X0IS12345678901", "2025-27", validFullRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          ("AA123456A", "X0IS12345678901", "2023-24", validFullRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          ("AA123456A", "X0IS12345678901", "2025-26", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          (
            "AA123456A",
            "X0IS12345678901",
            "2025-26",
            validFullRequestBodyJson.update(
              "/periodsOfAccountDates",
              Json.arr(
                Json.obj("startDate" -> "2025-04-06", "endDate" -> "2025-07-05"),
                Json.obj("startDate" -> "2025-06-06", "endDate" -> "2025-10-05")
              )
            ),
            BAD_REQUEST,
            RulePeriodsOverlapError.withPath("/periodsOfAccountDates"),
            None
          ),
          (
            "AA123456A",
            "X0IS12345678901",
            "2025-26",
            invalidRequestBodyJson,
            BAD_REQUEST,
            BadRequestError,
            Some(
              ErrorWrapper(
                "123",
                BadRequestError,
                Some(
                  List(
                    EndDateFormatError.withPath("/periodsOfAccountDates/0/endDate"),
                    StartDateFormatError.withPath("/periodsOfAccountDates/0/startDate"),
                    RuleEndDateError.withPath("/periodsOfAccountDates/2/endDate"),
                    RuleEndBeforeStartDateError.withPath("/periodsOfAccountDates/1"),
                    RulePeriodsOfAccountError,
                    RuleStartDateError.withPath("/periodsOfAccountDates/2/startDate")
                  )
                )
              )
            )
          )
        )

        input.foreach(validationErrorTest.tupled)
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamQueryParam, downstreamStatus, errorBody(downstreamCode))

            }

            val response: WSResponse = await(request().put(validFullRequestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        def errorBody(code: String): String =
          s"""
            |[
            |  {
            |    "errorCode": "$code",
            |    "errorDescription": "error description"
            |  }
            |]
          """.stripMargin

        val errors = List(
          (BAD_REQUEST, "1000", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "1007", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (UNPROCESSABLE_ENTITY, "1128", BAD_REQUEST, RuleEndBeforeStartDateError),
          (UNPROCESSABLE_ENTITY, "1129", BAD_REQUEST, RuleStartDateError),
          (UNPROCESSABLE_ENTITY, "1130", BAD_REQUEST, RuleEndDateError),
          (UNPROCESSABLE_ENTITY, "1131", BAD_REQUEST, RulePeriodsOverlapError),
          (UNPROCESSABLE_ENTITY, "1132", BAD_REQUEST, RuleCessationDateError),
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (NOT_IMPLEMENTED, "5000", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
          (NOT_FOUND, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError)
        )

        errors.foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {

    val nino: String       = "AA123456A"
    val businessId: String = "X0IS12345678901"
    val taxYear: String    = "2025-26"

    val downstreamQueryParam: Map[String, String] = Map("taxYear" -> "25-26")

    def downstreamUri: String = s"/itsd/income-sources/$nino/periods-of-account/$businessId"

    private def mtdUri: String = s"/$nino/$businessId/$taxYear/periods-of-account"

    val downstreamRequestBodyJson: JsValue = validFullRequestBodyJson.removeProperty("/periodsOfAccount")

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

}
