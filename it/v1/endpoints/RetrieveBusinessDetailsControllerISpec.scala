/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class RetrieveBusinessDetailsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"
    val businessId = "XAIS123456789012"

    val responseBody: JsValue = Json.parse(
      s"""
        |{
        |   "businessId": "XAIS123456789012",
        |   "typeOfBusiness": "self-employment",
        |   "tradingName": "RCDTS",
        |   "accountingPeriods": [
        |     {
        |       "start": "2001-01-01",
        |       "end": "2001-01-01"
        |     }
        |   ],
        |   "accountingType": "CASH",
        |   "commencementDate": "2001-01-01",
        |   "cessationDate": "2001-01-01",
        |   "businessAddressLineOne": "100 SuttonStreet",
        |   "businessAddressLineTwo": "Wokingham",
        |   "businessAddressLineThree": "Surrey",
        |   "businessAddressLineFour": "London",
        |   "businessAddressPostcode": "DH14EJ",
        |   "businessAddressCountryCode": "GB",
        |   "links": [
        |     {
        |       "href": "/individuals/business/details/$nino/$businessId",
        |       "method": "GET",
        |       "rel": "self"
        |     }
        |   ]
        |}
        |""".stripMargin
    )

    def setupStubs(): StubMapping

    def uri: String

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "Calling the retrieve business details endpoint" should {

    trait RetrieveBusinessDetailsControllerTest extends Test {
      def uri: String = s"/$nino/$businessId"

      def desUri: String = s"/registration/business-details/nino/$nino"
    }

    "return a 200 status code" when {
      "any valid request is made and single business returned" in new RetrieveBusinessDetailsControllerTest {

        val desJson: JsValue = Json.parse(
          """
            |{
            |  "safeId": "XAIS123456789012",
            |  "nino": "AA123456A",
            |  "mtdbsa": "123456789012345",
            |  "propertyIncome": false,
            |  "businessData": [{
            |    "incomeSourceId": "XAIS123456789012",
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
            |    "cashOrAccruals": "cash",
            |    "seasonal": true,
            |    "cessationDate": "2001-01-01",
            |    "cessationReason": "002",
            |    "paperLess": true
            |  }]
            |}
            |""".stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Status.OK, desJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made multiple business are returned" in new RetrieveBusinessDetailsControllerTest {

        val desJson: JsValue = Json.parse(
          """
            |{
            |  "safeId": "XE00001234567890",
            |  "nino": "AA123456A",
            |  "mtdbsa": "123456789012345",
            |  "propertyIncome": false,
            |  "businessData": [{
            |    "incomeSourceId": "XAIS123456789012",
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
            |    "cashOrAccruals": "cash",
            |    "seasonal": true,
            |    "cessationDate": "2001-01-01",
            |    "cessationReason": "002",
            |    "paperLess": true
            |  },
            |  {
            |    "incomeSourceId": "XAIS123456711112",
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
            |    "cashOrAccruals": "cash",
            |    "seasonal": true,
            |    "cessationDate": "2001-01-01",
            |    "cessationReason": "002",
            |    "paperLess": true
            |  }],
            |    "propertyData": [{
            |    "incomeSourceType": "foreign-property",
            |    "incomeSourceId": "XAIS176546789012",
            |    "accountingPeriodStartDate": "2019-04-06",
            |    "accountingPeriodEndDate": "2020-04-05",
            |    "tradingStartDate": "2017-07-24",
            |    "cashOrAccrualsFlag": true,
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
            |}
            |""".stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Status.OK, desJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, requestBusinessId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new RetrieveBusinessDetailsControllerTest {

            override val nino: String = requestNino
            override val businessId: String = requestBusinessId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }
        val input = Seq(
          ("AA1123A", "X0IS123456789012", Status.BAD_REQUEST, NinoFormatError),
          ("", "X0IS123456789012", Status.NOT_FOUND, NotFoundError),
          ("AA123456A", "X2", Status.BAD_REQUEST, BusinessIdFormatError)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }
      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new RetrieveBusinessDetailsControllerTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.GET, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.BAD_REQUEST, "INVALID_NINO", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_MTDBSA", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.NOT_FOUND, "NOT_FOUND_NINO", Status.NOT_FOUND, NotFoundError),
          (Status.NOT_FOUND, "NOT_FOUND_MTDBSA", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
