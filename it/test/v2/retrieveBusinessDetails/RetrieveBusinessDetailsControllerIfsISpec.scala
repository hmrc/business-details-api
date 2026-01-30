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

package v2.retrieveBusinessDetails

import api.models.errors.{BusinessIdFormatError, InternalError, MtdError, NinoFormatError, NotFoundError, RuleIncorrectGovTestScenarioError}
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class RetrieveBusinessDetailsControllerIfsISpec extends IntegrationBaseSpec {

  override def servicesConfig: Map[String, Any] =
    Map("feature-switch.ifs_hip_migration_1171.enabled" -> false) ++ super.servicesConfig

  "Calling the retrieve business details endpoint" should {

    "return a 200 status code" when {
      "any valid request is made and single business returned" in new Test {

        val downstreamJson: JsValue = Json.parse(
          """
            |{
            |"processingDate": "2023-07-05T09:16:58.655Z",
            |"taxPayerDisplayResponse": {
            |  "safeId": "XAIS123456789012",
            |  "nino": "AA123456A",
            |  "mtdbsa": "123456789012345",
            |  "yearOfMigration": "2023",
            |  "propertyIncome": false,
            |  "businessData": [{
            |    "incomeSourceId": "XAIS12345678901",
            |    "accountingPeriodStartDate": "2001-01-01",
            |    "accountingPeriodEndDate": "2001-01-01",
            |    "tradingName": "RCDTS",
            |    "businessAddressDetails": {
            |      "addressLine1": "100 SuttonStreet",
            |      "addressLine2": "Wokingham",
            |      "addressLine3": "Surrey",
            |      "addressLine4": "London",
            |      "postalCode": "DH14EJ",
            |      "countryCode": "GB"
            |    },
            |    "businessContactDetails": {
            |      "phoneNumber": "01332752856",
            |      "mobileNumber": "07782565326",
            |      "faxNumber": "01332754256",
            |      "emailAddress": "stephen@manncorpone.co.uk"
            |    },
            |    "tradingStartDate": "2001-01-01",
            |    "seasonal": true,
            |    "cessationDate": "2001-01-01",
            |    "cessationReason": "002",
            |    "paperLess": true,
            |    "firstAccountingPeriodStartDate": "2018-04-06",
            |    "firstAccountingPeriodEndDate": "2018-12-12",
            |    "latencyDetails": {
            |      "latencyEndDate": "2018-12-12",
            |      "taxYear1": "2018",
            |      "latencyIndicator1": "A",
            |      "taxYear2": "2019",
            |      "latencyIndicator2": "Q"
            |   },
            |   "quarterTypeElection": {
            |     "quarterReportingType": "STANDARD",
            |     "taxYearofElection": "2023"
            |   }
            |  }]
            |  }
            |}
            |""".stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, downstreamJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made multiple business are returned" in new Test {

        val downstreamJson: JsValue = Json.parse(
          """
            |{
            |"processingDate": "2023-07-05T09:16:58.655Z",
            |"taxPayerDisplayResponse": {
            |  "safeId": "XE00001234567890",
            |  "nino": "AA123456A",
            |  "mtdbsa": "123456789012345",
            |  "yearOfMigration": "2023",
            |  "propertyIncome": false,
            |  "businessData": [{
            |    "incomeSourceId": "XAIS12345678901",
            |    "accountingPeriodStartDate": "2001-01-01",
            |    "accountingPeriodEndDate": "2001-01-01",
            |    "tradingName": "RCDTS",
            |    "businessAddressDetails": {
            |      "addressLine1": "100 SuttonStreet",
            |      "addressLine2": "Wokingham",
            |      "addressLine3": "Surrey",
            |      "addressLine4": "London",
            |      "postalCode": "DH14EJ",
            |      "countryCode": "GB"
            |    },
            |    "businessContactDetails": {
            |      "phoneNumber": "01332752856",
            |      "mobileNumber": "07782565326",
            |      "faxNumber": "01332754256",
            |      "emailAddress": "stephen@manncorpone.co.uk"
            |    },
            |    "tradingStartDate": "2001-01-01",
            |    "seasonal": true,
            |    "cessationDate": "2001-01-01",
            |    "cessationReason": "002",
            |    "paperLess": true,
            |    "incomeSourceStartDate": "2019-07-14",
            |    "firstAccountingPeriodStartDate": "2018-04-06",
            |    "firstAccountingPeriodEndDate": "2018-12-12",
            |    "latencyDetails": {
            |      "latencyEndDate": "2018-12-12",
            |      "taxYear1": "2018",
            |      "latencyIndicator1": "A",
            |      "taxYear2": "2019",
            |      "latencyIndicator2": "Q"
            |     },
            |     "quarterTypeElection": {
            |          "quarterReportingType": "STANDARD",
            |          "taxYearofElection": "2023"
            |    }
            |  },
            |  {
            |    "incomeSourceId": "XAIS12345671111",
            |    "accountingPeriodStartDate": "2001-01-01",
            |    "accountingPeriodEndDate": "2001-01-01",
            |    "tradingName": "RCDTS",
            |    "businessAddressDetails": {
            |      "addressLine1": "100 SuttonStreet",
            |      "addressLine2": "Wokingham",
            |      "addressLine3": "Surrey",
            |      "addressLine4": "London",
            |      "postalCode": "DH14EJ",
            |      "countryCode": "GB"
            |    },
            |    "businessContactDetails": {
            |      "phoneNumber": "01332752856",
            |      "mobileNumber": "07782565326",
            |      "faxNumber": "01332754256",
            |      "emailAddress": "stephen@manncorpone.co.uk"
            |    },
            |    "tradingStartDate": "2001-01-01",
            |    "seasonal": true,
            |    "cessationDate": "2001-01-01",
            |    "cessationReason": "002",
            |    "paperLess": true
            |  }],
            |    "propertyData": [{
            |    "incomeSourceType": "foreign-property",
            |    "incomeSourceId": "XAIS17654678901",
            |    "accountingPeriodStartDate": "2019-04-06",
            |    "accountingPeriodEndDate": "2020-04-05",
            |    "tradingStartDate": "2017-07-24",
            |    "numPropRented": 0,
            |    "numPropRentedUK": 0,
            |    "numPropRentedEEA": 5,
            |    "numPropRentedNONEEA": 1,
            |    "emailAddress": "stephen@manncorpone.co.uk",
            |    "cessationDate": "2020-01-01",
            |    "cessationReason": "002",
            |    "paperLess": true,
            |    "incomeSourceStartDate": "2019-07-14"
            |  }]
            |  }
            |}
            |""".stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, downstreamJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, requestBusinessId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String       = requestNino
            override val businessId: String = requestBusinessId

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
          ("AA1123A", "X0IS123456789012", BAD_REQUEST, NinoFormatError),
          ("", "X0IS123456789012", NOT_FOUND, NotFoundError),
          ("AA123456A", "X2", BAD_REQUEST, BusinessIdFormatError)
        )
        input.foreach(validationErrorTest.tupled)
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_MTDBSA", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
          (NOT_FOUND, "NOT_FOUND_NINO", NOT_FOUND, NotFoundError),
          (NOT_FOUND, "NOT_FOUND_MTDBSA", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraIfsErrors = List(
          (BAD_REQUEST, "INVALID_MTD_ID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError)
        )

        (errors ++ extraIfsErrors).foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {

    val nino       = "AA123456A"
    val businessId = "XAIS12345678901"

    def uri           = s"/$nino/$businessId"
    def downstreamUri = s"/registration/business-details/nino/$nino"

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |   "businessId": "XAIS12345678901",
         |   "typeOfBusiness": "self-employment",
         |   "tradingName": "RCDTS",
         |   "accountingPeriods": [
         |     {
         |       "start": "2001-01-01",
         |       "end": "2001-01-01"
         |     }
         |   ],
         |   "commencementDate": "2001-01-01",
         |   "cessationDate": "2001-01-01",
         |   "businessAddressLineOne": "100 SuttonStreet",
         |   "businessAddressLineTwo": "Wokingham",
         |   "businessAddressLineThree": "Surrey",
         |   "businessAddressLineFour": "London",
         |   "businessAddressPostcode": "DH14EJ",
         |   "businessAddressCountryCode": "GB",
         |   "firstAccountingPeriodStartDate": "2018-04-06",
         |   "firstAccountingPeriodEndDate": "2018-12-12",
         |   "latencyDetails": {
         |     "latencyEndDate": "2018-12-12",
         |     "taxYear1": "2017-18",
         |     "latencyIndicator1": "A",
         |     "taxYear2": "2018-19",
         |     "latencyIndicator2": "Q"
         |   },
         |   "yearOfMigration": "2023",
         |   "quarterlyTypeChoice": {
         |     "quarterlyPeriodType": "standard",
         |     "taxYearOfChoice": "2022-23"
         |   }
         |}
         |""".stripMargin
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
         |      {
         |        "code": "$code",
         |        "reason": "message"
         |      }
    """.stripMargin

  }

}
