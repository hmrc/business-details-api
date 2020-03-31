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

package v1.models.response

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.domain.TypeOfBusiness
import v1.models.response.listAllBusiness.{Business, ListResponse}

class ListResponseSpec  extends UnitSpec {
  "reads" when {
    "passed DES json" should {
      "output a model" in {
        val desJson = Json.parse(
          """
            |{
            |   "safeId": "XE00001234567890",
            |   "nino": "AA123456A",
            |   "mtdbsa": "123456789012345",
            |   "propertyIncome": false,
            |   "businessData": [
            |      {
            |         "incomeSourceType": "1",
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
            |      }
            |   ]
            |}
            |""".stripMargin
        )
        val model = ListResponse(Seq(Business(TypeOfBusiness.`self-employment`,"123456789012345", Some("RCDTS"))))
        desJson.as[ListResponse] shouldBe model
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return mtd JSON" in {
        val model = ListResponse(Seq(Business(TypeOfBusiness.`self-employment`,"123456789012345", Some("RCDTS"))))
        val mtdJson = Json.parse(
          """
            |{
            |  "listOfBusinesses":[
            |     {
            |     "typeOfBusiness": "self-employment",
            |     "businessId": "123456789012345",
            |     "tradingName": "RCDTS"
            |     }
            |  ]
            |}
            |""".stripMargin
        )
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }
}