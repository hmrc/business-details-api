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

package v2.listAllBusinesses

import api.models.errors.{InternalError, MtdError, NinoFormatError, NotFoundError, RuleIncorrectGovTestScenarioError}
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class ListAllBusinessesControllerIfsISpec extends IntegrationBaseSpec {

  override def servicesConfig: Map[String, Any] =
    Map("feature-switch.ifs_hip_migration_1171.enabled" -> "false") ++ super.servicesConfig

  "Calling the list all businesses endpoint" should {

    trait ListAllBusinessesControllerTest extends Test {
      def uri: String    = s"/$nino/list"
      def desUri: String = s"/registration/business-details/nino/$nino"
    }

    "return a 200 status code" when {
      "any valid request is made and DES only returns businessData" in new ListAllBusinessesControllerTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, desUri, OK, downstreamResponseBodyBusinessData)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseBodyBusinessData
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made and DES only returns propertyData" in new ListAllBusinessesControllerTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, desUri, OK, downstreamResponseBodyPropertyData)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseBodyPropertyData
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made and DES returns both businessData and propertyData" in new ListAllBusinessesControllerTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, desUri, OK, downstreamResponseBodyBothData)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
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
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }
        val input = List(
          ("AA1123A", BAD_REQUEST, NinoFormatError),
          ("", NOT_FOUND, NotFoundError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }
      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new ListAllBusinessesControllerTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
              DownstreamStub.onError(DownstreamStub.GET, desUri, downstreamStatus, errorBody(downstreamCode))
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
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError)
        )
        (errors ++ extraIfsErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

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

    val downstreamResponseBodyBusinessData: JsValue = Json.parse(
      """
        |{
        | "processingDate": "2023-07-05T09:16:58.655Z",
        | "taxPayerDisplayResponse": {
        |   "safeId": "XE00001234567890",
        |   "nino": "AA123456A",
        |   "mtdbsa": "123456789012345",
        |   "propertyIncome": false,
        |   "businessData": [
        |     {
        |       "incomeSourceId": "123456789012345",
        |       "accountingPeriodStartDate": "2001-01-01",
        |       "accountingPeriodEndDate": "2001-01-01",
        |       "tradingName": "RCDTS",
        |       "businessAddressDetails": {
        |         "addressLine1": "100 SuttonStreet",
        |         "addressLine2": "Wokingham",
        |         "addressLine3": "Surrey",
        |         "addressLine4": "London",
        |         "postalCode": "DH14EJ",
        |         "countryCode": "GB"
        |       },
        |       "businessContactDetails": {
        |         "phoneNumber": "01332752856",
        |         "mobileNumber": "07782565326",
        |         "faxNumber": "01332754256",
        |         "emailAddress": "stephen@manncorpone.co.uk"
        |       },
        |       "tradingStartDate": "2001-01-01",
        |       "cashOrAccruals": "cash",
        |       "seasonal": true,
        |       "cessationDate": "2001-01-01",
        |       "cessationReason": "002",
        |       "paperLess": true
        |     }
        |   ]
        | }
        |}
        """.stripMargin
    )

    val downstreamResponseBodyPropertyData: JsValue = Json.parse(
      """
        |{
        | "processingDate": "2023-07-05T09:16:58.655Z",
        | "taxPayerDisplayResponse": {
        |   "safeId": "XE00001234567890",
        |   "nino": "AA123456A",
        |   "mtdbsa": "123456789012345",
        |   "propertyIncome": true,
        |   "propertyData": [
        |     {
        |       "incomeSourceType": "uk-property",
        |       "incomeSourceId": "098765432109876",
        |       "accountingPeriodStartDate": "2001-01-01",
        |       "accountingPeriodEndDate": "2001-01-01",
        |       "tradingStartDate": "2001-01-01",
        |       "cashOrAccrualsFlag": true,
        |       "numPropRented": 0,
        |       "numPropRentedUK": 0,
        |       "numPropRentedEEA": 5,
        |       "numPropRentedNONEEA": 1,
        |       "emailAddress": "stephen@manncorpone.co.uk",
        |       "cessationDate": "2001-01-01",
        |       "cessationReason": "002",
        |       "paperLess": true,
        |       "incomeSourceStartDate": "2019-07-14"
        |     }
        |   ]
        | }
        |}
        """.stripMargin
    )

    val downstreamResponseBodyBothData: JsValue = Json.parse(
      """
        |{
        | "processingDate": "2023-07-05T09:16:58.655Z",
        | "taxPayerDisplayResponse": {
        |   "safeId": "XE00001234567890",
        |   "nino": "AA123456A",
        |   "mtdbsa": "123456789012345",
        |   "propertyIncome": true,
        |   "businessData": [
        |     {
        |       "incomeSourceId": "123456789012345",
        |       "accountingPeriodStartDate": "2001-01-01",
        |       "accountingPeriodEndDate": "2001-01-01",
        |       "tradingName": "RCDTS",
        |       "businessAddressDetails": {
        |         "addressLine1": "100 SuttonStreet",
        |         "addressLine2": "Wokingham",
        |         "addressLine3": "Surrey",
        |         "addressLine4": "London",
        |         "postalCode": "DH14EJ",
        |         "countryCode": "GB"
        |       },
        |       "businessContactDetails": {
        |         "phoneNumber": "01332752856",
        |         "mobileNumber": "07782565326",
        |         "faxNumber": "01332754256",
        |         "emailAddress": "stephen@manncorpone.co.uk"
        |       },
        |       "tradingStartDate": "2001-01-01",
        |       "cashOrAccruals": "cash",
        |       "seasonal": true,
        |       "cessationDate": "2001-01-01",
        |       "cessationReason": "002",
        |       "paperLess": true
        |     }
        |   ],
        |   "propertyData": [
        |     {
        |       "incomeSourceType": "uk-property",
        |       "incomeSourceId": "098765432109876",
        |       "accountingPeriodStartDate": "2001-01-01",
        |       "accountingPeriodEndDate": "2001-01-01",
        |       "tradingStartDate": "2001-01-01",
        |       "cashOrAccrualsFlag": true,
        |       "numPropRented": 0,
        |       "numPropRentedUK": 0,
        |       "numPropRentedEEA": 5,
        |       "numPropRentedNONEEA": 1,
        |       "emailAddress": "stephen@manncorpone.co.uk",
        |       "cessationDate": "2001-01-01",
        |       "cessationReason": "002",
        |       "paperLess": true,
        |       "incomeSourceStartDate": "2019-07-14"
        |     }
        |   ]
        | }
        |}
        """.stripMargin
    )

    def setupStubs(): StubMapping

    def uri: String

    def request(): WSRequest = {
      AuthStub.resetAll()
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
         |        "reason": "des message"
         |      }
    """.stripMargin

  }

}
