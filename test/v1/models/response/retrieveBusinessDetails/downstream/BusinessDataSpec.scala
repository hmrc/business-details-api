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

import api.models.domain.{AccountingType, TaxYear}
import play.api.libs.json.Json
import support.UnitSpec

class BusinessDataSpec extends UnitSpec {

  "BusinessData" when {
    "read from JSON" must {
      "work" in {
        Json
          .parse(
            """
            |{
            |  "incomeSourceId": "XAIS12345678910",
            |  "accountingPeriodStartDate": "2001-01-01",
            |  "accountingPeriodEndDate": "2001-01-02",
            |  "firstAccountingPeriodStartDate": "2018-04-06",
            |  "firstAccountingPeriodEndDate":   "2018-12-12",
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
            |  "paperLess": true,
            |  "quarterTypeElection": {
            |   "quarterReportingType": "STANDARD",
            |   "taxYearofElection": "2023"
            |  }
            |}
            |""".stripMargin
          )
          .as[BusinessData] shouldBe
          BusinessData(
            incomeSourceId = "XAIS12345678910",
            tradingName = Some("RCDTS"),
            accountingPeriodStartDate = "2001-01-01",
            accountingPeriodEndDate = "2001-01-02",
            firstAccountingPeriodStartDate = Some("2018-04-06"),
            firstAccountingPeriodEndDate = Some("2018-12-12"),
            latencyDetails = Some(
              LatencyDetails(
                "2018-12-12",
                TaxYear.fromDownstream("2018"),
                LatencyIndicator.Annual,
                TaxYear.fromDownstream("2019"),
                LatencyIndicator.Quarterly)),
            cashOrAccruals = Some(AccountingType.CASH),
            tradingStartDate = Some("2001-01-01"),
            cessationDate = Some("2001-01-01"),
            businessAddressDetails = Some(
              BusinessAddressDetails(
                addressLine1 = "100 SuttonStreet",
                None,
                None,
                None,
                None,
                countryCode = "GB"
              )),
            quarterTypeElection = Some(QuarterTypeElection(QuarterReportingType.STANDARD, TaxYear.fromDownstream("2023")))
          )
      }

      "work for accountingType" when {
        def data(accountingType: Option[AccountingType]) =
          BusinessData(
            incomeSourceId = "XAIS12345678910",
            tradingName = None,
            accountingPeriodStartDate = "2001-01-01",
            accountingPeriodEndDate = "2001-01-02",
            firstAccountingPeriodStartDate = None,
            firstAccountingPeriodEndDate = None,
            latencyDetails = None,
            cashOrAccruals = accountingType,
            tradingStartDate = None,
            cessationDate = None,
            businessAddressDetails = None,
            quarterTypeElection = None
          )

        "using IFS (which has boolean cashOrAccruals)" when {
          def json(cashOrAccruals: Boolean) =
            Json.parse(
              s"""
                 |{
                 |  "incomeSourceId": "XAIS12345678910",
                 |  "accountingPeriodStartDate": "2001-01-01",
                 |  "accountingPeriodEndDate": "2001-01-02",
                 |  "cashOrAccruals": $cashOrAccruals
                 |}
                 |""".stripMargin
            )

          "field is true (accruals)" in {
            json(true).as[BusinessData] shouldBe data(Some(AccountingType.ACCRUALS))
          }

          "field is false (cash)" in {
            json(false).as[BusinessData] shouldBe data(Some(AccountingType.CASH))
          }

          "field is missing" in {
            Json
              .parse(
                s"""
                 |{
                 |  "incomeSourceId": "XAIS12345678910",
                 |  "accountingPeriodStartDate": "2001-01-01",
                 |  "accountingPeriodEndDate": "2001-01-02"
                 |}
                 |""".stripMargin
              )
              .as[BusinessData] shouldBe data(None)
          }
        }

        "using DES (which has string cashOrAccruals)" when {
          def json(cashOrAccruals: String) =
            Json.parse(
              s"""
                 |{
                 |  "incomeSourceId": "XAIS12345678910",
                 |  "accountingPeriodStartDate": "2001-01-01",
                 |  "accountingPeriodEndDate": "2001-01-02",
                 |  "cashOrAccruals": "$cashOrAccruals"
                 |}
                 |""".stripMargin
            )

          "field is accruals" in {
            json("accruals").as[BusinessData] shouldBe data(Some(AccountingType.ACCRUALS))
          }

          "field is cash" in {
            json("cash").as[BusinessData] shouldBe data(Some(AccountingType.CASH))
          }
        }

        "field is absent" in {
          Json
            .parse(
              s"""
               |{
               |  "incomeSourceId": "XAIS12345678910",
               |  "accountingPeriodStartDate": "2001-01-01",
               |  "accountingPeriodEndDate": "2001-01-02"
               |}
               |""".stripMargin
            )
            .as[BusinessData] shouldBe data(None)
        }
      }
    }
  }

}
