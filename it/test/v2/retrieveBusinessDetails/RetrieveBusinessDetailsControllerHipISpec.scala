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

import api.models.errors.*
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsArray, JsObject, JsPath, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import support.IntegrationBaseSpec

class RetrieveBusinessDetailsControllerHipISpec extends IntegrationBaseSpec {

  "Calling the retrieve business details endpoint" should {

    "return a 200 status code" when {
      "any valid request is made and a single business data is returned" in new Test {

        val downstreamJson: JsValue = updatedDownstreamJson("businessData")

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, downstreamJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe fullMtdJson("XAIS12345678901", "self-employment")
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made and multiple business data are returned" in new Test {

        override val businessId: String = "XAIS12345671111"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, fullDownstreamJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe fullMtdJson("XAIS12345671111", "self-employment")
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made and a single property data is returned" in new Test {

        override val businessId: String = "XPIS12345678901"
        val downstreamJson: JsValue     = updatedDownstreamJson("propertyData")

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, downstreamJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe updatedMtdJson("XPIS12345678901", "uk-property")
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made and multiple property data are returned" in new Test {

        override val businessId: String = "XFIS12345678903"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.authorised()
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, fullDownstreamJson)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
        response.json shouldBe updatedMtdJson("XFIS12345678903", "foreign-property")
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
          (UNPROCESSABLE_ENTITY, "001", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "006", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "007", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "008", NOT_FOUND, NoBusinessFoundError),
          (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError)
        )

        errors.foreach(serviceErrorTest.tupled)
      }
    }
  }

  private trait Test {

    val nino: String                               = "AA123456A"
    val businessId: String                         = "XAIS12345678901"
    val downstreamQueryParams: Map[String, String] = Map("nino" -> nino)

    private def uri: String   = s"/$nino/$businessId"
    def downstreamUri: String = "/etmp/RESTAdapter/itsa/taxpayer/business-details"

    val fullDownstreamJson: JsValue = Json.parse(
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
        |        },
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
        |          "incomeSourceType": "02",
        |          "incomeSourceId": "XPIS12345678901",
        |          "accPeriodSDate": "2001-01-01",
        |          "accPeriodEDate": "2001-01-01",
        |          "tradingSDate": "2001-01-01",
        |          "contextualTaxYear": "2024",
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
        |        },
        |        {
        |          "incomeSourceType": "03",
        |          "incomeSourceId": "XFIS12345678903",
        |          "accPeriodSDate": "2001-01-01",
        |          "accPeriodEDate": "2001-01-01",
        |          "tradingSDate": "2001-01-01",
        |          "contextualTaxYear": "2024",
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

    def updatedDownstreamJson(field: String): JsValue = fullDownstreamJson
      .transform(
        (JsPath \ "success" \ "taxPayerDisplayResponse" \ field).json.update(
          JsPath.read[JsArray].map { arr =>
            JsArray(arr.value.take(1))
          }
        )
      )
      .get

    def fullMtdJson(businessId: String, typeOfBusiness: String): JsValue = Json.parse(
      s"""
        |{
        |  "businessId": "$businessId",
        |  "typeOfBusiness": "$typeOfBusiness",
        |  "tradingName": "RCDTS",
        |  "accountingPeriods": [
        |    {
        |      "start": "2001-01-01",
        |      "end": "2001-01-01"
        |    }
        |  ],
        |  "commencementDate": "2001-01-01",
        |  "cessationDate": "2001-01-01",
        |  "businessAddressLineOne": "100 SuttonStreet",
        |  "businessAddressLineTwo": "Wokingham",
        |  "businessAddressLineThree": "Surrey",
        |  "businessAddressLineFour": "London",
        |  "businessAddressPostcode": "DH14EJ",
        |  "businessAddressCountryCode": "GB",
        |  "firstAccountingPeriodStartDate": "2018-04-06",
        |  "firstAccountingPeriodEndDate": "2018-12-12",
        |  "latencyDetails": {
        |    "latencyEndDate": "2018-12-12",
        |    "taxYear1": "2017-18",
        |    "latencyIndicator1": "A",
        |    "taxYear2": "2018-19",
        |    "latencyIndicator2": "Q"
        |  },
        |  "yearOfMigration": "2023",
        |  "quarterlyTypeChoice": {
        |    "quarterlyPeriodType": "standard",
        |    "taxYearOfChoice": "2022-23"
        |  }
        |}
      """.stripMargin
    )

    def updatedMtdJson(businessId: String, typeOfBusiness: String): JsValue = {
      val original: JsValue = fullMtdJson(businessId, typeOfBusiness)

      original.as[JsObject] -
        "tradingName" -
        "businessAddressLineOne" -
        "businessAddressLineTwo" -
        "businessAddressLineThree" -
        "businessAddressLineFour" -
        "businessAddressPostcode" -
        "businessAddressCountryCode"
    }

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
