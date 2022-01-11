/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import v1.models.errors.{DownstreamError, MtdError, NinoFormatError, NotFoundError}
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class ListAllBusinessesControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino = "AA123456A"

    val responseBodyBusinessData: JsValue = Json.parse(
      """
        |{
        |  "listOfBusinesses":[
        |    {
        |      "typeOfBusiness": "self-employment",
        |      "businessId": "123456789012345",
        |      "tradingName": "RCDTS",
        |      "links":[
        |        {
        |          "href":"/individuals/business/details/AA123456A/123456789012345",
        |          "method":"GET",
        |          "rel":"retrieve-business-details"
        |        }
        |      ]
        |    }
        |  ],
        |  "links":[
        |    {
        |      "href":"/individuals/business/details/AA123456A/list",
        |      "method":"GET",
        |      "rel":"self"
        |    }
        |  ]
        |}
        """.stripMargin
    )

    val responseBodyPropertyData: JsValue = Json.parse(
      """
        |{
        |  "listOfBusinesses":[
        |    {
        |      "typeOfBusiness": "uk-property",
        |      "businessId": "098765432109876",
        |      "links":[
        |        {
        |          "href":"/individuals/business/details/AA123456A/098765432109876",
        |          "method":"GET",
        |          "rel":"retrieve-business-details"
        |        }
        |      ]
        |    }
        |  ],
        |  "links":[
        |    {
        |      "href":"/individuals/business/details/AA123456A/list",
        |      "method":"GET",
        |      "rel":"self"
        |    }
        |  ]
        |}
        """.stripMargin
    )

    val responseBodyBothData: JsValue = Json.parse(
      """
        |{
        |  "listOfBusinesses":[
        |    {
        |      "typeOfBusiness": "self-employment",
        |      "businessId": "123456789012345",
        |      "tradingName": "RCDTS",
        |      "links":[
        |        {
        |          "href":"/individuals/business/details/AA123456A/123456789012345",
        |          "method":"GET",
        |          "rel":"retrieve-business-details"
        |        }
        |      ]
        |    },
        |    {
        |      "typeOfBusiness": "uk-property",
        |      "businessId": "098765432109876",
        |      "links":[
        |        {
        |          "href":"/individuals/business/details/AA123456A/098765432109876",
        |          "method":"GET",
        |          "rel":"retrieve-business-details"
        |        }
        |      ]
        |    }
        |  ],
        |  "links":[
        |    {
        |      "href":"/individuals/business/details/AA123456A/list",
        |      "method":"GET",
        |      "rel":"self"
        |    }
        |  ]
        |}
        """.stripMargin
    )

    val desResponseBodyBusinessData: JsValue = Json.parse(
      """
        |{
        |  "safeId": "XE00001234567890",
        |  "nino": "AA123456A",
        |  "mtdbsa": "123456789012345",
        |  "propertyIncome": false,
        |  "businessData": [
        |    {
        |      "incomeSourceId": "123456789012345",
        |      "accountingPeriodStartDate": "2001-01-01",
        |      "accountingPeriodEndDate": "2001-01-01",
        |      "tradingName": "RCDTS",
        |      "businessAddressDetails": {
        |        "addressLine1": "100 SuttonStreet",
        |        "addressLine2": "Wokingham",
        |        "addressLine3": "Surrey",
        |        "addressLine4": "London",
        |        "postalCode": "DH14EJ",
        |        "countryCode": "GB"
        |      },
        |      "businessContactDetails": {
        |        "phoneNumber": "01332752856",
        |        "mobileNumber": "07782565326",
        |        "faxNumber": "01332754256",
        |        "emailAddress": "stephen@manncorpone.co.uk"
        |      },
        |      "tradingStartDate": "2001-01-01",
        |      "cashOrAccruals": "cash",
        |      "seasonal": true,
        |      "cessationDate": "2001-01-01",
        |      "cessationReason": "002",
        |      "paperLess": true
        |    }
        |  ]
        |}
        """.stripMargin
    )

    val desResponseBodyPropertyData: JsValue = Json.parse(
      """
        |{
        |  "safeId": "XE00001234567890",
        |  "nino": "AA123456A",
        |  "mtdbsa": "123456789012345",
        |  "propertyIncome": true,
        |  "propertyData": [
        |    {
        |      "incomeSourceType": "uk-property",
        |      "incomeSourceId": "098765432109876",
        |      "accountingPeriodStartDate": "2001-01-01",
        |      "accountingPeriodEndDate": "2001-01-01",
        |      "tradingStartDate": "2001-01-01",
        |      "cashOrAccrualsFlag": true,
        |      "numPropRented": 0,
        |      "numPropRentedUK": 0,
        |      "numPropRentedEEA": 5,
        |      "numPropRentedNONEEA": 1,
        |      "emailAddress": "stephen@manncorpone.co.uk",
        |      "cessationDate": "2001-01-01",
        |      "cessationReason": "002",
        |      "paperLess": true,
        |      "incomeSourceStartDate": "2019-07-14"
        |    }
        |  ]
        |}
        """.stripMargin
    )

    val desResponseBodyBothData: JsValue = Json.parse(
      """
        |{
        |  "safeId": "XE00001234567890",
        |  "nino": "AA123456A",
        |  "mtdbsa": "123456789012345",
        |  "propertyIncome": true,
        |  "businessData": [
        |    {
        |      "incomeSourceId": "123456789012345",
        |      "accountingPeriodStartDate": "2001-01-01",
        |      "accountingPeriodEndDate": "2001-01-01",
        |      "tradingName": "RCDTS",
        |      "businessAddressDetails": {
        |        "addressLine1": "100 SuttonStreet",
        |        "addressLine2": "Wokingham",
        |        "addressLine3": "Surrey",
        |        "addressLine4": "London",
        |        "postalCode": "DH14EJ",
        |        "countryCode": "GB"
        |      },
        |      "businessContactDetails": {
        |        "phoneNumber": "01332752856",
        |        "mobileNumber": "07782565326",
        |        "faxNumber": "01332754256",
        |        "emailAddress": "stephen@manncorpone.co.uk"
        |      },
        |      "tradingStartDate": "2001-01-01",
        |      "cashOrAccruals": "cash",
        |      "seasonal": true,
        |      "cessationDate": "2001-01-01",
        |      "cessationReason": "002",
        |      "paperLess": true
        |    }
        |  ],
        |  "propertyData": [
        |    {
        |      "incomeSourceType": "uk-property",
        |      "incomeSourceId": "098765432109876",
        |      "accountingPeriodStartDate": "2001-01-01",
        |      "accountingPeriodEndDate": "2001-01-01",
        |      "tradingStartDate": "2001-01-01",
        |      "cashOrAccrualsFlag": true,
        |      "numPropRented": 0,
        |      "numPropRentedUK": 0,
        |      "numPropRentedEEA": 5,
        |      "numPropRentedNONEEA": 1,
        |      "emailAddress": "stephen@manncorpone.co.uk",
        |      "cessationDate": "2001-01-01",
        |      "cessationReason": "002",
        |      "paperLess": true,
        |      "incomeSourceStartDate": "2019-07-14"
        |    }
        |  ]
        |}
        """.stripMargin
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

  "Calling the list all businesses endpoint" should {

    trait ListAllBusinessesControllerTest extends Test {
      def uri: String = s"/$nino/list"
      def desUri: String = s"/registration/business-details/nino/$nino"
    }

    "return a 200 status code" when {
      "any valid request is made and DES only returns businessData" in new ListAllBusinessesControllerTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Status.OK, desResponseBodyBusinessData)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBodyBusinessData
        response.header("Content-Type") shouldBe Some("application/json")
      }
      "any valid request is made and DES only returns propertyData" in new ListAllBusinessesControllerTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Status.OK, desResponseBodyPropertyData)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBodyPropertyData
        response.header("Content-Type") shouldBe Some("application/json")
      }
      "any valid request is made and DES returns both businessData and propertyData" in new ListAllBusinessesControllerTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Status.OK, desResponseBodyBothData)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBodyBothData
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new ListAllBusinessesControllerTest {

            override val nino: String = requestNino

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
          ("AA1123A", Status.BAD_REQUEST, NinoFormatError),
          ("", Status.NOT_FOUND, NotFoundError)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }
      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new ListAllBusinessesControllerTest {

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
          (Status.NOT_FOUND, "NOT_FOUND_NINO", Status.NOT_FOUND, NotFoundError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
