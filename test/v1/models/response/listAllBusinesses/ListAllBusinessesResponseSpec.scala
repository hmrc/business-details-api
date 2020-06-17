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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.models.response.listAllBusinesses

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.domain.TypeOfBusiness
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesResponse}

class ListAllBusinessesResponseSpec  extends UnitSpec {
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
            |         "cashOrAccruals": "cash",
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
            |         "cashOrAccruals": "cash",
            |         "seasonal": true,
            |         "cessationDate": "2001-01-01",
            |         "cessationReason": "002",
            |         "paperLess": true
            |      }
            |   ]
            |}
            |""".stripMargin
        )
        val model = ListAllBusinessesResponse(Seq(
          Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("RCDTS")),
          Business(TypeOfBusiness.`self-employment`, "098765432109876", Some("RCDTS 2"))
        ))
        desJson.as[ListAllBusinessesResponse] shouldBe model
      }
      "passed DES json with propertyData" in {
        val desJson = Json.parse(
          """
            |{
            |   "safeId": "XE00001234567890",
            |   "nino": "AA123456A",
            |   "mtdbsa": "123456789012345",
            |   "propertyIncome": false,
            |   "propertyData": [
            |      {
            |         "incomeSourceType": "uk-property",
            |         "incomeSourceId": "123456789012345",
            |         "accountingPeriodStartDate": "2001-01-01",
            |         "accountingPeriodEndDate": "2001-01-01",
            |         "tradingName": "doesn't matter",
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
            |         "cashOrAccruals": "cash",
            |         "seasonal": true,
            |         "cessationDate": "2001-01-01",
            |         "cessationReason": "002",
            |         "paperLess": true
            |      },
            |      {
            |         "incomeSourceType": "uk-property",
            |         "incomeSourceId": "098765432109876",
            |         "accountingPeriodStartDate": "2001-01-01",
            |         "accountingPeriodEndDate": "2001-01-01",
            |         "tradingName": "doesn't matter",
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
            |         "cashOrAccruals": "cash",
            |         "seasonal": true,
            |         "cessationDate": "2001-01-01",
            |         "cessationReason": "002",
            |         "paperLess": true
            |      }
            |   ]
            |}
            |""".stripMargin
        )
        val model = ListAllBusinessesResponse(Seq(
          Business(TypeOfBusiness.`uk-property` ,"123456789012345", None),
          Business(TypeOfBusiness.`uk-property` ,"098765432109876", None)
        ))
        desJson.as[ListAllBusinessesResponse] shouldBe model
      }
      "passed DES json with businessData and propertyData" in {
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
            |         "cashOrAccruals": "cash",
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
            |         "cashOrAccruals": "cash",
            |         "seasonal": true,
            |         "cessationDate": "2001-01-01",
            |         "cessationReason": "002",
            |         "paperLess": true
            |      }
            |   ],
            |   "propertyData": [
            |      {
            |         "incomeSourceType": "uk-property",
            |         "incomeSourceId": "123456789012345",
            |         "accountingPeriodStartDate": "2001-01-01",
            |         "accountingPeriodEndDate": "2001-01-01",
            |         "tradingName": "doesn't matter",
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
            |         "cashOrAccruals": "cash",
            |         "seasonal": true,
            |         "cessationDate": "2001-01-01",
            |         "cessationReason": "002",
            |         "paperLess": true
            |      },
            |      {
            |         "incomeSourceType": "uk-property",
            |         "incomeSourceId": "098765432109876",
            |         "accountingPeriodStartDate": "2001-01-01",
            |         "accountingPeriodEndDate": "2001-01-01",
            |         "tradingName": "doesn't matter",
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
            |         "cashOrAccruals": "cash",
            |         "seasonal": true,
            |         "cessationDate": "2001-01-01",
            |         "cessationReason": "002",
            |         "paperLess": true
            |      }
            |   ]
            |}
            |""".stripMargin
        )
        val model = ListAllBusinessesResponse(Seq(
          Business(TypeOfBusiness.`self-employment`,"123456789012345", Some("RCDTS")),
          Business(TypeOfBusiness.`self-employment`,"098765432109876", Some("RCDTS 2")),
          Business(TypeOfBusiness.`uk-property` ,"123456789012345", None),
          Business(TypeOfBusiness.`uk-property` ,"098765432109876", None)
        ))
        desJson.as[ListAllBusinessesResponse] shouldBe model
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return mtd JSON" in {
        val model = ListAllBusinessesResponse(Seq(
          Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("name")),
          Business(TypeOfBusiness.`uk-property`, "123456789012346", None)
        ))
        val mtdJson = Json.parse(
          s"""
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
}