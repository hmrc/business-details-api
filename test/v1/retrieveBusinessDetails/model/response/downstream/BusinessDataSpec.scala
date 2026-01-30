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

import api.models.domain.TaxYear
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class BusinessDataSpec extends UnitSpec {

  private val model = BusinessData(
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

  def json(isHip: Boolean): JsValue = {
    val accPeriodStartDateField: String = if (isHip) "accPeriodSDate" else "accountingPeriodStartDate"
    val accPeriodEndDateField: String   = if (isHip) "accPeriodEDate" else "accountingPeriodEndDate"
    val tradingStartDateField: String   = if (isHip) "tradingSDate" else "tradingStartDate"

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
        ("HIP", true),
        ("IFS", false)
      ).foreach { case (downstreamName, isHip) =>
        s"work when and downstream is $downstreamName" in {
          json(
            isHip = isHip
          ).as[BusinessData] shouldBe model
        }
      }
    }
  }

}
