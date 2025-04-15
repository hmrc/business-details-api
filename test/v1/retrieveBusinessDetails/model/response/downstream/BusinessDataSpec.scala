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

package v1.retrieveBusinessDetails.model.response.downstream

import api.models.domain.{AccountingType, TaxYear}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class BusinessDataSpec extends UnitSpec {

  private def model(accountingType: AccountingType) = BusinessData(
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
    cashOrAccruals = Some(accountingType),
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

  def json(cashOrAccrualsFlag: Boolean, isHip: Boolean): JsValue = {
    val accPeriodStartDateField: String = if (isHip) "accPeriodSDate" else "accountingPeriodStartDate"
    val accPeriodEndDateField: String   = if (isHip) "accPeriodEDate" else "accountingPeriodEndDate"
    val tradingStartDateField: String   = if (isHip) "tradingSDate" else "tradingStartDate"
    val cashOrAccrualsField: String     = if (isHip) "cashOrAccrualsFlag" else "cashOrAccruals"

    Json.parse(
      s"""
        |{
        |  "incomeSourceId": "XAIS12345678910",
        |  "$accPeriodStartDateField": "2001-01-01",
        |  "$accPeriodEndDateField": "2001-01-02",
        |  "firstAccountingPeriodStartDate": "2018-04-06",
        |  "firstAccountingPeriodEndDate": "2018-12-12",
        |  "latencyDetails": {
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
        |  "$tradingStartDateField": "2001-01-01",
        |  "$cashOrAccrualsField": $cashOrAccrualsFlag,
        |  "seasonal": true,
        |  "cessationDate": "2001-01-01",
        |  "cessationReason": "002",
        |  "paperLess": true,
        |  "quarterTypeElection": {
        |    "quarterReportingType": "STANDARD",
        |    "taxYearofElection": "2023"
        |  }
        |}
      """.stripMargin
    )
  }

  "BusinessData" when {
    "read from JSON" must {
      Seq(
        (AccountingType.ACCRUALS, true),
        (AccountingType.CASH, false)
      ).foreach { case (accountingType, flagValue) =>
        Seq(
          ("HIP", true, "cashOrAccrualsFlag"),
          ("IFS", false, "cashOrAccruals")
        ).foreach { case (downstreamName, isHip, flagName) =>
          s"work when accountingType is $accountingType, $flagName is $flagValue and downstream is $downstreamName" in {
            json(
              cashOrAccrualsFlag = flagValue,
              isHip = isHip
            ).as[BusinessData] shouldBe model(accountingType = accountingType)
          }
        }
      }

      "work for accountingType" when {
        def data(accountingType: Option[AccountingType]): BusinessData =
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
          def json(cashOrAccruals: Boolean): JsValue =
            Json.parse(
              s"""
                |{
                |  "incomeSourceId": "XAIS12345678910",
                |  "accountingPeriodStartDate": "2001-01-01",
                |  "accountingPeriodEndDate": "2001-01-02",
                |  "cashOrAccruals": $cashOrAccruals
                |}
              """.stripMargin
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
                """
                  |{
                  |  "incomeSourceId": "XAIS12345678910",
                  |  "accountingPeriodStartDate": "2001-01-01",
                  |  "accountingPeriodEndDate": "2001-01-02"
                  |}
                """.stripMargin
              )
              .as[BusinessData] shouldBe data(None)
          }
        }

        "using DES (which has string cashOrAccruals)" when {
          def json(cashOrAccruals: String): JsValue =
            Json.parse(
              s"""
                |{
                |  "incomeSourceId": "XAIS12345678910",
                |  "accountingPeriodStartDate": "2001-01-01",
                |  "accountingPeriodEndDate": "2001-01-02",
                |  "cashOrAccruals": "$cashOrAccruals"
                |}
              """.stripMargin
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
              """
                |{
                |  "incomeSourceId": "XAIS12345678910",
                |  "accountingPeriodStartDate": "2001-01-01",
                |  "accountingPeriodEndDate": "2001-01-02"
                |}
              """.stripMargin
            )
            .as[BusinessData] shouldBe data(None)
        }
      }
    }
  }

}
