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

package v1.models.response.retrieveBusinessDetails.downstream

import api.models.domain.{AccountingType, TaxYear, TypeOfBusiness}
import config.MockFeatureSwitches
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.response.retrieveBusinessDetails.AccountingPeriod

class BusinessDetailsSpec extends UnitSpec with MockFeatureSwitches {


  "reads" should {
    "read from business json" when {
      "A full json is supplied" in {
        MockFeatureSwitches.isIfsEnabled.returns(true).anyNumberOfTimes()

        Json.parse(
          """
            |{
            |  "incomeSourceId": "XAIS12345678910",
            |  "accountingPeriodStartDate": "2001-01-01",
            |  "accountingPeriodEndDate": "2001-01-02",
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
        ).as[BusinessDetails](BusinessDetails.readsBusinessData) shouldBe
          BusinessDetails(
            businessId = "XAIS12345678910",
            typeOfBusiness = TypeOfBusiness.`self-employment`,
            tradingName = Some("RCDTS"),
            accountingPeriods = Seq(AccountingPeriod("2001-01-01", "2001-01-02")),
            firstAccountingPeriodStartDate = Some("2018-04-06"),
            firstAccountingPeriodEndDate = Some("2018-12-12"),
            latencyDetails = Some(LatencyDetails("2018-12-12", TaxYear.fromDownstream("2018"), LatencyIndicator.Annual, TaxYear.fromDownstream("2019"), LatencyIndicator.Quarterly)),
            yearOfMigration = Some("2023"),
            accountingType = AccountingType.CASH,
            commencementDate = Some("2001-01-01"),
            cessationDate = Some("2001-01-01"),
            businessAddressLineOne = Some("100 SuttonStreet"),
            businessAddressLineTwo = Some("Wokingham"),
            businessAddressLineThree = Some("Surrey"),
            businessAddressLineFour = Some("London"),
            businessAddressPostcode = Some("DH14EJ"),
            businessAddressCountryCode = Some("GB")
          )
      }
    }

    "read from property json" when {
      "A full json is supplied" in {
        MockFeatureSwitches.isIfsEnabled.returns(true).anyNumberOfTimes()

        Json.parse(
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
        ).as[BusinessDetails](BusinessDetails.readsPropertyData) shouldBe
          BusinessDetails(
            businessId = "X0IS123456789012",
            typeOfBusiness = TypeOfBusiness.`foreign-property`,
            tradingName = None,
            accountingPeriods = Seq(AccountingPeriod("2019-04-06", "2020-04-05")),
            firstAccountingPeriodStartDate = Some("2018-04-06"),
            firstAccountingPeriodEndDate = Some("2018-12-12"),
            latencyDetails = Some(LatencyDetails("2018-12-12", TaxYear.fromDownstream("2018"), LatencyIndicator.Annual, TaxYear.fromDownstream("2019"), LatencyIndicator.Quarterly)),
            yearOfMigration = Some("2023"),
            accountingType = AccountingType.ACCRUALS,
            commencementDate = Some("2017-07-24"),
            cessationDate = Some("2020-01-01"),
            businessAddressLineOne = None,
            businessAddressLineTwo = None,
            businessAddressLineThree = None,
            businessAddressLineFour = None,
            businessAddressPostcode = None,
            businessAddressCountryCode = None
          )
      }

      "A partial json is supplied" in {
        MockFeatureSwitches.isIfsEnabled.returns(true).anyNumberOfTimes()

        Json.parse(
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
        ).as[BusinessDetails](BusinessDetails.readsPropertyData) shouldBe BusinessDetails(
          businessId = "X0IS123456789012",
          typeOfBusiness = TypeOfBusiness.`property-unspecified`,
          tradingName = None,
          accountingPeriods = Seq(AccountingPeriod("2019-04-06", "2020-04-05")),
          firstAccountingPeriodStartDate = Some("2018-04-06"),
          firstAccountingPeriodEndDate = Some("2018-12-12"),
          latencyDetails = Some(LatencyDetails("2018-12-12", TaxYear.fromDownstream("2018"), LatencyIndicator.Annual, TaxYear.fromDownstream("2019"), LatencyIndicator.Quarterly)),
          yearOfMigration = Some("2023"),
          accountingType = AccountingType.CASH,
          commencementDate = None,
          cessationDate = None,
          businessAddressLineOne = None,
          businessAddressLineTwo = None,
          businessAddressLineThree = None,
          businessAddressLineFour = None,
          businessAddressPostcode = None,
          businessAddressCountryCode = None
        )
      }

      "A the minimum json is supplied" in {
        MockFeatureSwitches.isIfsEnabled.returns(true).anyNumberOfTimes()

        Json.parse(
          """
            |{
            |  "incomeSourceId": "X0IS123456789012",
            |  "accountingPeriodStartDate": "2019-04-06",
            |  "accountingPeriodEndDate": "2020-04-05"
            |}
            |""".stripMargin
        ).as[BusinessDetails](BusinessDetails.readsPropertyData) shouldBe
          BusinessDetails(
            businessId = "X0IS123456789012",
            typeOfBusiness = TypeOfBusiness.`property-unspecified`,
            tradingName = None,
            accountingPeriods = Seq(AccountingPeriod("2019-04-06", "2020-04-05")),
            firstAccountingPeriodStartDate = None,
            firstAccountingPeriodEndDate = None,
            latencyDetails = None,
            yearOfMigration = None,
            accountingType = AccountingType.CASH,
            commencementDate = None,
            cessationDate = None,
            businessAddressLineOne = None,
            businessAddressLineTwo = None,
            businessAddressLineThree = None,
            businessAddressLineFour = None,
            businessAddressPostcode = None,
            businessAddressCountryCode = None
          )
      }
    }
  }

}
