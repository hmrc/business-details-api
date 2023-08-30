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

package v1.models.response.RetrieveBusinessDetails.downstream

import api.models.domain.TypeOfBusiness
import api.models.domain.accountingType.AccountingType
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.response.retrieveBusinessDetails.AccountingPeriod
import v1.models.response.retrieveBusinessDetails.downstream.{BusinessDetails, LatencyDetails, LatencyIndicator}

class BusinessDetailsSpec extends UnitSpec {

  "reads" should {
    "read from business json" when {
      "A full json is supplied" in {

        val businessDesJson: JsValue = Json.parse(
          """
            |{
            |  "incomeSourceId": "XAIS12345678910",
            |  "accountingPeriodStartDate": "2001-01-01",
            |  "accountingPeriodEndDate": "2001-01-01",
            |  "firstAccountingPeriodStartDate": "2018-04-06",
            |  "firstAccountingPeriodEndDate":   "2018-12-12",
            |  "yearOfMigration": "2023",
            |  "latencyDetails":  {
            |    "taxYear1": "2018",
            |    "taxYear2": "2019",
            |    "latencyIndicator1": "A",
            |    "latencyIndicator2": "Q",
            |    "latencyEndDate": "2018-12-12"
            |  },
            |  "tradingName": "RCDTS",
            |  "businessAddressDetails": {
            |    "addressLine1": "100 SuttonStreet",
            |    "addressLine2": "Wokingham",
            |    "addressLine3": "Surrey",
            |    "addressLine4": "London",
            |    "postalCode": "DH14EJ",
            |    "countryCode": "GB"
            |  },
            |  "businessContactDetails": {
            |    "phoneNumber": "01332752856",
            |    "mobileNumber": "07782565326",
            |    "faxNumber": "01332754256",
            |    "emailAddress": "stephen@manncorpone.co.uk"
            |  },
            |  "tradingStartDate": "2001-01-01",
            |  "cashOrAccruals": false,
            |  "seasonal": true,
            |  "cessationDate": "2001-01-01",
            |  "cessationReason": "002",
            |  "paperLess": true
            |}
            |""".stripMargin
        )

        val responseBody = BusinessDetails(
          "XAIS12345678910",
          TypeOfBusiness.`self-employment`,
          Some("RCDTS"),
          Seq(AccountingPeriod("2001-01-01", "2001-01-01")),
          Some("2018-04-06"),
          Some("2018-12-12"),
          Some(LatencyDetails("2018-12-12", "2018", LatencyIndicator.Annual, "2019", LatencyIndicator.Quarterly)),
          Some("2023"),
          Some(AccountingType.CASH),
          Some("2001-01-01"),
          Some("2001-01-01"),
          Some("100 SuttonStreet"),
          Some("Wokingham"),
          Some("Surrey"),
          Some("London"),
          Some("DH14EJ"),
          Some("GB")
        )
        businessDesJson.as[BusinessDetails](BusinessDetails.readsBusinessData.apply(true)) shouldBe responseBody
      }
    }
    "read from property json" when {
      "A full json is supplied" in {

        val propertyDesJson: JsValue = Json.parse(
          """
            |{
            |  "incomeSourceType": "foreign-property",
            |  "incomeSourceId": "X0IS123456789012",
            |  "accountingPeriodStartDate": "2019-04-06",
            |  "accountingPeriodEndDate": "2020-04-05",
            |  "tradingStartDate": "2017-07-24",
            |  "firstAccountingPeriodStartDate": "2018-04-06",
            |  "firstAccountingPeriodEndDate":   "2018-12-12",
            |  "yearOfMigration": "2023",
            |  "latencyDetails":  {
            |    "taxYear1": "2018",
            |    "taxYear2": "2019",
            |    "latencyIndicator1": "A",
            |    "latencyIndicator2": "Q",
            |    "latencyEndDate": "2018-12-12"
            |  },
            |  "cashOrAccruals": true,
            |  "numPropRented": 0,
            |  "numPropRentedUK": 0,
            |  "numPropRentedEEA": 5,
            |  "numPropRentedNONEEA": 1,
            |  "emailAddress": "stephen@manncorpone.co.uk",
            |  "cessationDate": "2020-01-01",
            |  "cessationReason": "002",
            |  "paperLess": true,
            |  "incomeSourceStartDate": "2019-07-14"
            |}
            |""".stripMargin
        )

        val responseBody = BusinessDetails(
          "X0IS123456789012",
          TypeOfBusiness.`foreign-property`,
          None,
          Seq(AccountingPeriod("2019-04-06", "2020-04-05")),
          Some("2018-04-06"),
          Some("2018-12-12"),
          Some(LatencyDetails("2018-12-12", "2018", LatencyIndicator.Annual, "2019", LatencyIndicator.Quarterly)),
          Some("2023"),
          Some(AccountingType.ACCRUALS),
          Some("2017-07-24"),
          Some("2020-01-01"),
          None,
          None,
          None,
          None,
          None,
          None
        )

        propertyDesJson.as[BusinessDetails](BusinessDetails.readsPropertyData.apply(true)) shouldBe responseBody
      }

      "A partial json is supplied" in {

        val propertyDesJson: JsValue = Json.parse(
          """
            |{
            |  "incomeSourceId": "X0IS123456789012",
            |  "accountingPeriodStartDate": "2019-04-06",
            |  "accountingPeriodEndDate": "2020-04-05",
            |  "cashOrAccruals": false,
            |  "numPropRented": 0,
            |  "numPropRentedUK": 0,
            |  "numPropRentedEEA": 5,
            |  "numPropRentedNONEEA": 1,
            |  "emailAddress": "stephen@manncorpone.co.uk",
            |  "cessationReason": "002",
            |  "paperLess": true,
            |  "incomeSourceStartDate": "2019-07-14",
            |  "firstAccountingPeriodStartDate": "2018-04-06",
            |  "firstAccountingPeriodEndDate":   "2018-12-12",
            |  "yearOfMigration": "2023",
            |  "latencyDetails":  {
            |    "taxYear1": "2018",
            |    "taxYear2": "2019",
            |    "latencyIndicator1": "A",
            |    "latencyIndicator2": "Q",
            |    "latencyEndDate": "2018-12-12"
            |  }
            |}
            |""".stripMargin
        )

        val responseBody = BusinessDetails(
          "X0IS123456789012",
          TypeOfBusiness.`property-unspecified`,
          None,
          Seq(AccountingPeriod("2019-04-06", "2020-04-05")),
          Some("2018-04-06"),
          Some("2018-12-12"),
          Some(LatencyDetails("2018-12-12", "2018", LatencyIndicator.Annual, "2019", LatencyIndicator.Quarterly)),
          Some("2023"),
          Some(AccountingType.CASH),
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )

        propertyDesJson.as[BusinessDetails](BusinessDetails.readsPropertyData.apply(true)) shouldBe responseBody
      }
      "A the minimum json is supplied" in {

        val propertyDesJson: JsValue = Json.parse(
          """
            |{
            |  "incomeSourceId": "X0IS123456789012",
            |  "accountingPeriodStartDate": "2019-04-06",
            |  "accountingPeriodEndDate": "2020-04-05"
            |}
            |""".stripMargin
        )

        val responseBody = BusinessDetails(
          "X0IS123456789012",
          TypeOfBusiness.`property-unspecified`,
          None,
          Seq(AccountingPeriod("2019-04-06", "2020-04-05")),
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )

        propertyDesJson.as[BusinessDetails](BusinessDetails.readsPropertyData.apply(true)) shouldBe responseBody
      }
    }
  }

}
