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

package v1.listAllBusinesses

import api.models.errors.*
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import support.IntegrationBaseSpec

class ListAllBusinessesControllerHipISpec extends IntegrationBaseSpec {

  "Calling the list all businesses endpoint" should {

    trait ListAllBusinessesControllerTest extends Test {
      def uri: String                                = s"/$nino/list"
      def downstreamUri: String                      = "/etmp/RESTAdapter/itsa/taxpayer/business-details"
      val downstreamQueryParams: Map[String, String] = Map("nino" -> nino)
    }

    "return a 200 status code" when {
      "any valid request is made and downstream only returns businessData" in new ListAllBusinessesControllerTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, downstreamResponseBodyBusinessData)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseBodyBusinessData
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made and downstream only returns propertyData" in new ListAllBusinessesControllerTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, downstreamResponseBodyPropertyData)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe responseBodyPropertyData
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made and downstream returns both businessData and propertyData" in new ListAllBusinessesControllerTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, downstreamResponseBodyBothData)
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

        input.foreach(validationErrorTest.tupled)
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new ListAllBusinessesControllerTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              MtdIdLookupStub.ninoFound(nino)
              AuthStub.authorised()
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (UNPROCESSABLE_ENTITY, "001", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "006", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "007", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "008", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError)
        )

        errors.foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {

    val nino = "AA123456A"

    val responseBodyBusinessData: JsValue = Json.parse(
      """
        |{
        |  "listOfBusinesses": [
        |    {
        |      "typeOfBusiness": "self-employment",
        |      "businessId": "XAIS12345678901",
        |      "tradingName": "RCDTS",
        |      "links": [
        |        {
        |          "href": "/individuals/business/details/AA123456A/XAIS12345678901",
        |          "method": "GET",
        |          "rel": "retrieve-business-details"
        |        }
        |      ]
        |    }
        |  ],
        |  "links": [
        |    {
        |      "href": "/individuals/business/details/AA123456A/list",
        |      "method": "GET",
        |      "rel": "self"
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val responseBodyPropertyData: JsValue = Json.parse(
      """
        |{
        |  "listOfBusinesses": [
        |    {
        |      "typeOfBusiness": "uk-property",
        |      "businessId": "XPIS12345678901",
        |      "links": [
        |        {
        |          "href": "/individuals/business/details/AA123456A/XPIS12345678901",
        |          "method": "GET",
        |          "rel": "retrieve-business-details"
        |        }
        |      ]
        |    }
        |  ],
        |  "links": [
        |    {
        |      "href": "/individuals/business/details/AA123456A/list",
        |      "method": "GET",
        |      "rel": "self"
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val responseBodyBothData: JsValue = Json.parse(
      """
        |{
        |  "listOfBusinesses": [
        |    {
        |      "typeOfBusiness": "self-employment",
        |      "businessId": "XAIS12345671111",
        |      "tradingName": "RCDTS",
        |      "links": [
        |        {
        |          "href": "/individuals/business/details/AA123456A/XAIS12345671111",
        |          "method": "GET",
        |          "rel": "retrieve-business-details"
        |        }
        |      ]
        |    },
        |    {
        |      "typeOfBusiness": "foreign-property",
        |      "businessId": "XFIS12345678903",
        |      "links": [
        |        {
        |          "href": "/individuals/business/details/AA123456A/XFIS12345678903",
        |          "method": "GET",
        |          "rel": "retrieve-business-details"
        |        }
        |      ]
        |    }
        |  ],
        |  "links": [
        |    {
        |      "href": "/individuals/business/details/AA123456A/list",
        |      "method": "GET",
        |      "rel": "self"
        |    }
        |  ]
        |}
      """.stripMargin
    )

    val downstreamResponseBodyBusinessData: JsValue = Json.parse(
      """
        |{
        |  "success": {
        |    "processingDate": "2023-07-05T09:16:58Z",
        |    "taxPayerDisplayResponse": {
        |      "safeId": "XAIS123456789012",
        |      "nino": "AA123456A",
        |      "mtdId": "XNIT00000068707",
        |      "yearOfMigration": "2023",
        |      "propertyIncomeFlag": false,
        |      "businessData": [
        |        {
        |          "incomeSourceId": "XAIS12345678901",
        |          "incomeSource": "ITSB",
        |          "accPeriodSDate": "2001-01-01",
        |          "accPeriodEDate": "2001-01-01",
        |          "tradingName": "RCDTS",
        |          "businessAddressDetails": {
        |            "addressLine1": "100 SuttonStreet",
        |            "addressLine2": "Wokingham",
        |            "addressLine3": "Surrey",
        |            "addressLine4": "London",
        |            "postalCode": "DH14EJ",
        |            "countryCode": "GB"
        |          },
        |          "businessContactDetails": {
        |            "telephone": "01332752856",
        |            "mobileNo": "07782565326",
        |            "faxNo": "01332754256",
        |            "email": "stephen@manncorpone.co.uk"
        |          },
        |          "tradingSDate": "2001-01-01",
        |          "contextualTaxYear": "2024",
        |          "cashOrAccrualsFlag": false,
        |          "seasonalFlag": true,
        |          "cessationDate": "2001-01-01",
        |          "paperLessFlag": true,
        |          "incomeSourceStartDate": "2010-03-14",
        |          "firstAccountingPeriodStartDate": "2018-04-06",
        |          "firstAccountingPeriodEndDate": "2018-12-12",
        |          "latencyDetails": {
        |            "latencyEndDate": "2018-12-12",
        |            "taxYear1": "2018",
        |            "latencyIndicator1": "A",
        |            "taxYear2": "2019",
        |            "latencyIndicator2": "Q"
        |          },
        |          "quarterTypeElection": {
        |            "quarterReportingType": "STANDARD",
        |            "taxYearofElection": "2023"
        |          }
        |        }
        |      ]
        |    }
        |  }
        |}
      """.stripMargin
    )

    val downstreamResponseBodyPropertyData: JsValue = Json.parse(
      """
        |{
        |  "success": {
        |    "processingDate": "2023-07-05T09:16:58Z",
        |    "taxPayerDisplayResponse": {
        |      "safeId": "XAIS123456789012",
        |      "nino": "AA123456A",
        |      "mtdId": "XNIT00000068707",
        |      "yearOfMigration": "2023",
        |      "propertyIncomeFlag": false,
        |      "propertyData": [
        |        {
        |          "incomeSourceType": "02",
        |          "incomeSourceId": "XPIS12345678901",
        |          "accPeriodSDate": "2001-01-01",
        |          "accPeriodEDate": "2001-01-01",
        |          "tradingSDate": "2001-01-01",
        |          "contextualTaxYear": "2024",
        |          "cashOrAccrualsFlag": false,
        |          "numPropRented": 0,
        |          "numPropRentedUK": 0,
        |          "numPropRentedEEA": 5,
        |          "numPropRentedNONEEA": 1,
        |          "email": "stephen@manncorpone.co.uk",
        |          "cessationDate": "2001-01-01",
        |          "paperLessFlag": true,
        |          "incomeSourceStartDate": "2019-07-14",
        |          "firstAccountingPeriodStartDate": "2018-04-06",
        |          "firstAccountingPeriodEndDate": "2018-12-12",
        |          "latencyDetails": {
        |            "latencyEndDate": "2018-12-12",
        |            "taxYear1": "2018",
        |            "latencyIndicator1": "A",
        |            "taxYear2": "2019",
        |            "latencyIndicator2": "Q"
        |          },
        |          "quarterTypeElection": {
        |            "quarterReportingType": "STANDARD",
        |            "taxYearofElection": "2023"
        |          }
        |        }
        |      ]
        |    }
        |  }
        |}
      """.stripMargin
    )

    val downstreamResponseBodyBothData: JsValue = Json.parse(
      """
        |{
        |  "success": {
        |    "processingDate": "2023-07-05T09:16:58Z",
        |    "taxPayerDisplayResponse": {
        |      "safeId": "XAIS123456789012",
        |      "nino": "AA123456A",
        |      "mtdId": "XNIT00000068707",
        |      "yearOfMigration": "2023",
        |      "propertyIncomeFlag": false,
        |      "businessData": [
        |        {
        |          "incomeSourceId": "XAIS12345671111",
        |          "incomeSource": "ITSB",
        |          "accPeriodSDate": "2001-01-01",
        |          "accPeriodEDate": "2001-01-01",
        |          "tradingName": "RCDTS",
        |          "businessAddressDetails": {
        |            "addressLine1": "100 SuttonStreet",
        |            "addressLine2": "Wokingham",
        |            "addressLine3": "Surrey",
        |            "addressLine4": "London",
        |            "postalCode": "DH14EJ",
        |            "countryCode": "GB"
        |          },
        |          "businessContactDetails": {
        |            "telephone": "01332752856",
        |            "mobileNo": "07782565326",
        |            "faxNo": "01332754256",
        |            "email": "stephen@manncorpone.co.uk"
        |          },
        |          "tradingSDate": "2001-01-01",
        |          "contextualTaxYear": "2024",
        |          "cashOrAccrualsFlag": false,
        |          "seasonalFlag": true,
        |          "cessationDate": "2001-01-01",
        |          "paperLessFlag": true,
        |          "incomeSourceStartDate": "2010-03-14",
        |          "firstAccountingPeriodStartDate": "2018-04-06",
        |          "firstAccountingPeriodEndDate": "2018-12-12",
        |          "latencyDetails": {
        |            "latencyEndDate": "2018-12-12",
        |            "taxYear1": "2018",
        |            "latencyIndicator1": "A",
        |            "taxYear2": "2019",
        |            "latencyIndicator2": "Q"
        |          },
        |          "quarterTypeElection": {
        |            "quarterReportingType": "STANDARD",
        |            "taxYearofElection": "2023"
        |          }
        |        }
        |      ],
        |      "propertyData": [
        |        {
        |          "incomeSourceType": "03",
        |          "incomeSourceId": "XFIS12345678903",
        |          "accPeriodSDate": "2001-01-01",
        |          "accPeriodEDate": "2001-01-01",
        |          "tradingSDate": "2001-01-01",
        |          "contextualTaxYear": "2024",
        |          "cashOrAccrualsFlag": false,
        |          "numPropRented": 0,
        |          "numPropRentedUK": 0,
        |          "numPropRentedEEA": 5,
        |          "numPropRentedNONEEA": 1,
        |          "email": "stephen@manncorpone.co.uk",
        |          "cessationDate": "2001-01-01",
        |          "paperLessFlag": true,
        |          "incomeSourceStartDate": "2019-07-14",
        |          "firstAccountingPeriodStartDate": "2018-04-06",
        |          "firstAccountingPeriodEndDate": "2018-12-12",
        |          "latencyDetails": {
        |            "latencyEndDate": "2018-12-12",
        |            "taxYear1": "2018",
        |            "latencyIndicator1": "A",
        |            "taxYear2": "2019",
        |            "latencyIndicator2": "Q"
        |          },
        |          "quarterTypeElection": {
        |            "quarterReportingType": "STANDARD",
        |            "taxYearofElection": "2023"
        |          }
        |        }
        |      ]
        |    }
        |  }
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
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
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
