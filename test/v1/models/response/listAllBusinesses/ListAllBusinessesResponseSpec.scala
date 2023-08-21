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

package v1.models.response.listAllBusinesses

import api.hateoas.HateoasFactory
import api.models.domain.TypeOfBusiness
import api.models.hateoas.Method.GET
import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import api.models.hateoas.{HateoasWrapper, Link}
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesHateoasData, ListAllBusinessesResponse}

class ListAllBusinessesResponseSpec extends UnitSpec {

  "reads" should {
    "output a model" when {
      "passed DES json with businessData" in {
        val desJson = Json.parse(
          """
            |{
            |   "safeId": "XE00001234567890",
            |   "nino": "AA123456A",
            |   "mtdbsa": "123456789012345",
            |   "propertyIncome": false,
            |   "businessData": [
            |      {
            |         "incomeSourceType": "doesn't matter",
            |         "incomeSourceId": "123456789012345",
            |         "accountingPeriodStartDate": "2001-01-01",
            |         "accountingPeriodEndDate": "2001-01-01",
            |         "tradingName": "RCDTS",
            |         "businessAddressDetails": {
            |            "addressLine1": "100 SuttonStreet",
            |            "addressLine2": "Wokingham",
            |            "addressLine3": "Surrey",
            |            "addressLine4": "London",
            |            "postalCode": "DH14EJ",
            |            "countryCode": "GB"
            |         },
            |         "businessContactDetails": {
            |            "phoneNumber": "01332752856",
            |            "mobileNumber": "07782565326",
            |            "faxNumber": "01332754256",
            |            "emailAddress": "stephen@manncorpone.co.uk"
            |         },
            |         "tradingStartDate": "2001-01-01",
            |         "cashOrAccruals": false,
            |         "seasonal": true,
            |         "cessationDate": "2001-01-01",
            |         "cessationReason": "002",
            |         "paperLess": true
            |      },
            |      {
            |         "incomeSourceType": "doesn't matter",
            |         "incomeSourceId": "098765432109876",
            |         "accountingPeriodStartDate": "2001-01-01",
            |         "accountingPeriodEndDate": "2001-01-01",
            |         "tradingName": "RCDTS 2",
            |         "businessAddressDetails": {
            |            "addressLine1": "100 SuttonStreet",
            |            "addressLine2": "Wokingham",
            |            "addressLine3": "Surrey",
            |            "addressLine4": "London",
            |            "postalCode": "DH14EJ",
            |            "countryCode": "GB"
            |         },
            |         "businessContactDetails": {
            |            "phoneNumber": "01332752856",
            |            "mobileNumber": "07782565326",
            |            "faxNumber": "01332754256",
            |            "emailAddress": "stephen@manncorpone.co.uk"
            |         },
            |         "tradingStartDate": "2001-01-01",
            |         "cashOrAccruals": false,
            |         "seasonal": true,
            |         "cessationDate": "2001-01-01",
            |         "cessationReason": "002",
            |         "paperLess": true
            |      }
            |   ]
            |}
            |""".stripMargin
        )
        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("RCDTS")),
            Business(TypeOfBusiness.`self-employment`, "098765432109876", Some("RCDTS 2"))
          ))
        desJson.as[ListAllBusinessesResponse[Business]] shouldBe model
      }
      "passed DES json with propertyData" in {
        val desJson = Json.parse(
          """
            |{
            |  "safeId": "XE00001234567890",
            |  "nino": "AA123456A",
            |  "mtdbsa": "123456789012345",
            |  "propertyIncome": true,
            |  "propertyData": [
            |    {
            |      "incomeSourceType": "uk-property",
            |      "incomeSourceId": "123456789012345",
            |      "accountingPeriodStartDate": "2001-01-01",
            |      "accountingPeriodEndDate": "2001-01-01",
            |      "tradingStartDate": "2001-01-01",
            |      "cashOrAccruals": true,
            |      "numPropRented": 0,
            |      "numPropRentedUK": 0,
            |      "numPropRentedEEA": 5,
            |      "numPropRentedNONEEA": 1,
            |      "emailAddress": "stephen@manncorpone.co.uk",
            |      "cessationDate": "2001-01-01",
            |      "cessationReason": "002",
            |      "paperLess": true,
            |      "incomeSourceStartDate": "2019-07-14"
            |    },
            |    {
            |      "incomeSourceType": "foreign-property",
            |      "incomeSourceId": "098765432109876",
            |      "accountingPeriodStartDate": "2001-01-01",
            |      "accountingPeriodEndDate": "2001-01-01",
            |      "tradingStartDate": "2001-01-01",
            |      "cashOrAccruals": true,
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
            |""".stripMargin
        )
        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`uk-property`, "123456789012345", None),
            Business(TypeOfBusiness.`foreign-property`, "098765432109876", None)
          ))
        desJson.as[ListAllBusinessesResponse[Business]] shouldBe model
      }
      "passed DES json with businessData and propertyData" in {
        val desJson = Json.parse(
          """
            |{
            |  "safeId": "XE00001234567890",
            |  "nino": "AA123456A",
            |  "mtdbsa": "123456789012345",
            |  "propertyIncome": true,
            |  "businessData": [
            |    {
            |      "incomeSourceType": "doesn't matter",
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
            |      "cashOrAccruals": false,
            |      "seasonal": true,
            |      "cessationDate": "2001-01-01",
            |      "cessationReason": "002",
            |      "paperLess": true
            |    },
            |    {
            |      "incomeSourceType": "doesn't matter",
            |      "incomeSourceId": "098765432109876",
            |      "accountingPeriodStartDate": "2001-01-01",
            |      "accountingPeriodEndDate": "2001-01-01",
            |      "tradingName": "RCDTS 2",
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
            |      "cashOrAccruals": false,
            |      "seasonal": true,
            |      "cessationDate": "2001-01-01",
            |      "cessationReason": "002",
            |      "paperLess": true
            |    }
            |  ],
            |  "propertyData": [
            |    {
            |      "incomeSourceType": "uk-property",
            |      "incomeSourceId": "123456789012345",
            |      "accountingPeriodStartDate": "2001-01-01",
            |      "accountingPeriodEndDate": "2001-01-01",
            |      "tradingStartDate": "2001-01-01",
            |      "cashOrAccruals": true,
            |      "numPropRented": 0,
            |      "numPropRentedUK": 0,
            |      "numPropRentedEEA": 5,
            |      "numPropRentedNONEEA": 1,
            |      "emailAddress": "stephen@manncorpone.co.uk",
            |      "cessationDate": "2001-01-01",
            |      "cessationReason": "002",
            |      "paperLess": true,
            |      "incomeSourceStartDate": "2019-07-14"
            |    },
            |    {
            |      "incomeSourceType": "foreign-property",
            |      "incomeSourceId": "098765432109876",
            |      "accountingPeriodStartDate": "2001-01-01",
            |      "accountingPeriodEndDate": "2001-01-01",
            |      "tradingStartDate": "2001-01-01",
            |      "cashOrAccruals": true,
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
            |""".stripMargin
        )
        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("RCDTS")),
            Business(TypeOfBusiness.`self-employment`, "098765432109876", Some("RCDTS 2")),
            Business(TypeOfBusiness.`uk-property`, "123456789012345", None),
            Business(TypeOfBusiness.`foreign-property`, "098765432109876", None)
          ))
        desJson.as[ListAllBusinessesResponse[Business]] shouldBe model
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return mtd JSON" in {
        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("name")),
            Business(TypeOfBusiness.`uk-property`, "123456789012346", None)
          ))
        val mtdJson = Json.parse(s"""
             |{
             |  "listOfBusinesses": [
             |    {
             |      "typeOfBusiness": "self-employment",
             |      "businessId": "123456789012345",
             |      "tradingName": "name"
             |    },
             |    {
             |      "typeOfBusiness": "uk-property",
             |      "businessId": "123456789012346"
             |    }
             |  ]
             |}
             |""".stripMargin)
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

  "HateoasFactory" must {
    class Test extends MockAppConfig {
      val hateoasFactory = new HateoasFactory(mockAppConfig)
      val nino           = "someNino"
      MockAppConfig.apiGatewayContext.returns("individuals/business/details").anyNumberOfTimes()
    }

    "expose the correct links for list" in new Test {
      hateoasFactory.wrapList(
        ListAllBusinessesResponse(Seq(Business(TypeOfBusiness.`self-employment`, "myid", None))),
        ListAllBusinessesHateoasData(nino)) shouldBe
        HateoasWrapper(
          ListAllBusinessesResponse(
            Seq(
              HateoasWrapper(
                Business(TypeOfBusiness.`self-employment`, "myid", None),
                Seq(Link(s"/individuals/business/details/$nino/myid", GET, "retrieve-business-details"))))),
          Seq(
            Link(s"/individuals/business/details/$nino/list", GET, "self")
          )
        )
    }
  }

}
