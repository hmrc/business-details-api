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

import api.models.domain.{TaxYear, TypeOfBusiness}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class PropertyDataSpec extends UnitSpec {

  private def model(typeOfBusiness: TypeOfBusiness) = PropertyData(
    incomeSourceType = Some(typeOfBusiness),
    incomeSourceId = "X0IS123456789012",
    accountingPeriodStartDate = "2019-04-06",
    accountingPeriodEndDate = "2020-04-05",
    firstAccountingPeriodStartDate = Some("2018-04-06"),
    firstAccountingPeriodEndDate = Some("2018-12-12"),
    latencyDetails = Some(
      LatencyDetails(
        "2018-12-12",
        TaxYear.fromDownstream("2018"),
        LatencyIndicator.Annual,
        TaxYear.fromDownstream("2019"),
        LatencyIndicator.Quarterly)),
    tradingStartDate = Some("2017-07-24"),
    cessationDate = Some("2020-01-01"),
    quarterTypeElection = Some(QuarterTypeElection(QuarterReportingType.STANDARD, TaxYear.fromDownstream("2023")))
  )

  def json(incomeSourceType: String, isHip: Boolean): JsValue = {
    val accPeriodStartDateField: String = if (isHip) "accPeriodSDate" else "accountingPeriodStartDate"
    val accPeriodEndDateField: String   = if (isHip) "accPeriodEDate" else "accountingPeriodEndDate"
    val tradingStartDateField: String   = if (isHip) "tradingSDate" else "tradingStartDate"

    Json.parse(
      s"""
        |{
        |  "incomeSourceType": "$incomeSourceType",
        |  "incomeSourceId": "X0IS123456789012",
        |  "$accPeriodStartDateField": "2019-04-06",
        |  "$accPeriodEndDateField": "2020-04-05",
        |  "$tradingStartDateField": "2017-07-24",
        |  "firstAccountingPeriodStartDate": "2018-04-06",
        |  "firstAccountingPeriodEndDate": "2018-12-12",
        |  "latencyDetails": {
        |    "taxYear1": "2018",
        |    "taxYear2": "2019",
        |    "latencyIndicator1": "A",
        |    "latencyIndicator2": "Q",
        |    "latencyEndDate": "2018-12-12"
        |  },
        |  "numPropRented": 0,
        |  "numPropRentedUK": 0,
        |  "numPropRentedEEA": 5,
        |  "numPropRentedNONEEA": 1,
        |  "emailAddress": "stephen@manncorpone.co.uk",
        |  "cessationDate": "2020-01-01",
        |  "cessationReason": "002",
        |  "paperLess": true,
        |  "incomeSourceStartDate": "2019-07-14",
        |  "quarterTypeElection": {
        |    "quarterReportingType": "STANDARD",
        |    "taxYearofElection": "2023"
        |  }
        |}
      """.stripMargin
    )
  }

  "PropertyData" when {
    "read from JSON" must {
      Seq(
        (TypeOfBusiness.`uk-property`, "02", "uk-property"),
        (TypeOfBusiness.`foreign-property`, "03", "foreign-property")
      ).foreach { case (typeOfBusiness, hipIncomeSourceType, ifsIncomeSourceType) =>
        Seq(
          ("HIP", true, hipIncomeSourceType),
          ("IFS", false, ifsIncomeSourceType)
        ).foreach { case (downstreamName, isHip, incomeSourceType) =>
          s"work when typeOfBusiness is $typeOfBusiness " +
            s"incomeSourceType is $incomeSourceType and downstream is $downstreamName" in {
              json(
                incomeSourceType = incomeSourceType,
                isHip = isHip
              ).as[PropertyData] shouldBe model(typeOfBusiness = typeOfBusiness)
            }
        }
      }
    }
  }

}
