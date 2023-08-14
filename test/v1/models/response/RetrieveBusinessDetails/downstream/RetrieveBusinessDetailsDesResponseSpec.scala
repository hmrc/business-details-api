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
import v1.models.response.retrieveBusinessDetails.downstream.{BusinessDetails, LatencyDetails, LatencyIndicator, RetrieveBusinessDetailsDownstreamResponse}
import v1.models.response.retrieveBusinessDetails.AccountingPeriod

class RetrieveBusinessDetailsDesResponseSpec extends UnitSpec {

  "reads" should {
    "read business data from json" when {
      "A full json is supplied" in {

        val desJson: JsValue = Json.parse(
          """
            |{
            |  "safeId": "XE00001234567890",
            |  "nino": "AA123456A",
            |  "mtdbsa": "123456789012345",
            |  "yearOfMigration": "2023",
            |  "propertyIncome": false,
            |  "businessData": [{
            |    "incomeSourceId": "XAIS12345678910",
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
            |    "firstAccountingPeriodStartDate": "2018-04-06",
            |    "firstAccountingPeriodEndDate":   "2018-12-12",
            |    "latencyDetails":  {
            |      "taxYear1": "2018",
            |      "taxYear2": "2019",
            |      "latencyIndicator1": "A",
            |      "latencyIndicator2": "Q",
            |      "latencyEndDate": "2018-12-12"
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

        val responseBody: RetrieveBusinessDetailsDownstreamResponse = RetrieveBusinessDetailsDownstreamResponse(
          Seq(BusinessDetails(
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
          )))

        responseBody shouldBe desJson.as[RetrieveBusinessDetailsDownstreamResponse]
      }
    }
    "read property data from json" when {
      "A full json is supplied" in {

        val desJson: JsValue = Json.parse(
          """
            |{
            |  "safeId": "XE00001234567890",
            |  "nino": "AA123456A",
            |  "mtdbsa": "123456789012345",
            |  "propertyIncome": false,
            |  "yearOfMigration": "2023",
            |  "propertyData": [{
            |    "incomeSourceType": "foreign-property",
            |    "incomeSourceId": "X0IS123456789012",
            |    "accountingPeriodStartDate": "2019-04-06",
            |    "accountingPeriodEndDate": "2020-04-05",
            |    "tradingStartDate": "2017-07-24",
            |    "cashOrAccrualsFlag": true,
            |    "numPropRented": 0,
            |    "firstAccountingPeriodStartDate": "2018-04-06",
            |    "firstAccountingPeriodEndDate":   "2018-12-12",
            |    "yearOfMigration": "2023",
            |    "latencyDetails":  {
            |      "taxYear1": "2018",
            |      "taxYear2": "2019",
            |      "latencyIndicator1": "A",
            |      "latencyIndicator2": "Q",
            |      "latencyEndDate": "2018-12-12"
            |    },
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

        val responseBody: RetrieveBusinessDetailsDownstreamResponse = RetrieveBusinessDetailsDownstreamResponse(
          Seq(BusinessDetails(
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
          )))

        responseBody shouldBe desJson.as[RetrieveBusinessDetailsDownstreamResponse]
      }
    }
  }

}
