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

package v1.models.response.RetrieveBusinessDetails.des

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.domain.TypeOfBusiness
import v1.models.domain.accountingType.AccountingType
import v1.models.response.retrieveBusinessDetails.AccountingPeriod
import v1.models.response.retrieveBusinessDetails.des.BusinessDetails

class BusinessDetailsSpec extends UnitSpec {

  "reads" should {
    "read from json" when {
      "A full json is supplied" in {

        val desJson: JsValue = Json.parse(
          """
            |    {
            |      "incomeSourceType": "1",
            |      "incomeSourceId": "X0IS123456789012",
            |      "accountingPeriods": [{
            |        "accountingPeriodStartDate": "2001-01-01",
            |        "accountingPeriodEndDate": "2001-01-01"
            |      }],
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
            |""".stripMargin
        )

        val responseBody = BusinessDetails(
            "X0IS123456789012",
            TypeOfBusiness.`self-employment`,
            Some("RCDTS"),
            Some(Seq(AccountingPeriod("2001-01-01", "2001-01-01"))),
            AccountingType.CASH,
            Some("2001-01-01"),
            Some("2001-01-01"),
            Some("100 SuttonStreet"),
            Some("Wokingham"),
            Some("Surrey"),
            Some("London"),
            Some("DH14EJ"),
            Some("GB")
          )

        responseBody shouldBe desJson.as[BusinessDetails]
      }
    }
  }


}