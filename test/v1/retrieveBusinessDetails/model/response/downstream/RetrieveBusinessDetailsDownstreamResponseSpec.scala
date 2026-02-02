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
import play.api.libs.json.Json
import support.UnitSpec

class RetrieveBusinessDetailsDownstreamResponseSpec extends UnitSpec {

  "RetrieveBusinessDetailsDownstreamResponse" when {
    "reads" should {
      "read the response from DES" when {
        "only business data is supplied" in {
          val result = Json
            .parse(
              """
                |{
                |  "safeId": "XE00001234567890",
                |  "nino": "AA123456A",
                |  "mtdbsa": "123456789012345",
                |  "yearOfMigration": "2023",
                |  "propertyIncome": false,
                |  "businessData": [
                |    {
                |      "incomeSourceId": "XAIS12345678910",
                |      "accountingPeriodStartDate": "2001-01-01",
                |      "accountingPeriodEndDate": "2001-01-02",
                |      "quarterTypeElection": {
                |        "quarterReportingType": "STANDARD",
                |        "taxYearofElection": "2024"
                |      }
                |    }
                |  ]
                |}
              """.stripMargin
            )
            .as[RetrieveBusinessDetailsDownstreamResponse]

          val expected = RetrieveBusinessDetailsDownstreamResponse(
            yearOfMigration = Some("2023"),
            businessData = Some(
              List(BusinessData(
                incomeSourceId = "XAIS12345678910",
                tradingName = None,
                accountingPeriodStartDate = "2001-01-01",
                accountingPeriodEndDate = "2001-01-02",
                firstAccountingPeriodStartDate = None,
                firstAccountingPeriodEndDate = None,
                latencyDetails = None,
                tradingStartDate = None,
                cessationDate = None,
                businessAddressDetails = None,
                quarterTypeElection = Some(QuarterTypeElection(QuarterReportingType.STANDARD, TaxYear.fromMtd("2023-24")))
              ))),
            propertyData = None
          )

          result shouldBe expected
        }

        "only property data is supplied" in {
          val result = Json
            .parse(
              """
                |{
                |  "safeId": "XE00001234567890",
                |  "nino": "AA123456A",
                |  "mtdbsa": "123456789012345",
                |  "propertyIncome": true,
                |  "yearOfMigration": "2023",
                |  "propertyData": [
                |    {
                |      "incomeSourceId": "X0IS123456789012",
                |      "accountingPeriodStartDate": "2019-04-06",
                |      "accountingPeriodEndDate": "2020-04-05"
                |    }
                |  ]
                |}
              """.stripMargin
            )
            .as[RetrieveBusinessDetailsDownstreamResponse]

          val expected = RetrieveBusinessDetailsDownstreamResponse(
            yearOfMigration = Some("2023"),
            businessData = None,
            propertyData = Some(
              List(PropertyData(
                incomeSourceType = None,
                incomeSourceId = "X0IS123456789012",
                accountingPeriodStartDate = "2019-04-06",
                accountingPeriodEndDate = "2020-04-05",
                firstAccountingPeriodStartDate = None,
                firstAccountingPeriodEndDate = None,
                latencyDetails = None,
                tradingStartDate = None,
                cessationDate = None,
                quarterTypeElection = None
              )))
          )

          result shouldBe expected
        }
      }

      "read the response from IFS" when {
        "only business data is supplied" in {
          val result = Json
            .parse(
              """
                |{
                |  "processingDate": "2023-07-05T09:16:58.655Z",
                |  "taxPayerDisplayResponse": {
                |    "safeId": "XE00001234567890",
                |    "nino": "AA123456A",
                |    "mtdbsa": "123456789012345",
                |    "yearOfMigration": "2023",
                |    "propertyIncome": false,
                |    "businessData": [
                |      {
                |        "incomeSourceId": "XAIS12345678910",
                |        "accountingPeriodStartDate": "2001-01-01",
                |        "accountingPeriodEndDate": "2001-01-02"
                |      }
                |    ]
                |  }
                |}
              """.stripMargin
            )
            .as[RetrieveBusinessDetailsDownstreamResponse]

          val expected = RetrieveBusinessDetailsDownstreamResponse(
            yearOfMigration = Some("2023"),
            businessData = Some(
              List(BusinessData(
                incomeSourceId = "XAIS12345678910",
                tradingName = None,
                accountingPeriodStartDate = "2001-01-01",
                accountingPeriodEndDate = "2001-01-02",
                firstAccountingPeriodStartDate = None,
                firstAccountingPeriodEndDate = None,
                latencyDetails = None,
                tradingStartDate = None,
                cessationDate = None,
                businessAddressDetails = None,
                quarterTypeElection = None
              ))),
            propertyData = None
          )

          result shouldBe expected
        }

        "only property data is supplied" in {
          val result = Json
            .parse(
              """
                |{
                |  "processingDate": "2023-07-05T09:16:58.655Z",
                |  "taxPayerDisplayResponse": {
                |    "safeId": "XE00001234567890",
                |    "nino": "AA123456A",
                |    "mtdbsa": "123456789012345",
                |    "propertyIncome": true,
                |    "yearOfMigration": "2023",
                |    "propertyData": [
                |      {
                |        "incomeSourceId": "X0IS123456789012",
                |        "accountingPeriodStartDate": "2019-04-06",
                |        "accountingPeriodEndDate": "2020-04-05"
                |      }
                |    ]
                |  }
                |}
              """.stripMargin
            )
            .as[RetrieveBusinessDetailsDownstreamResponse]

          val expected = RetrieveBusinessDetailsDownstreamResponse(
            yearOfMigration = Some("2023"),
            businessData = None,
            propertyData = Some(
              List(PropertyData(
                incomeSourceType = None,
                incomeSourceId = "X0IS123456789012",
                accountingPeriodStartDate = "2019-04-06",
                accountingPeriodEndDate = "2020-04-05",
                firstAccountingPeriodStartDate = None,
                firstAccountingPeriodEndDate = None,
                latencyDetails = None,
                tradingStartDate = None,
                cessationDate = None,
                quarterTypeElection = None
              )))
          )

          result shouldBe expected
        }
      }

      "read the response from HIP" when {
        "only business data is supplied" in {
          val result = Json
            .parse(
              """
                |{
                |  "success": {
                |    "processingDate": "2023-07-05T09:16:58Z",
                |    "taxPayerDisplayResponse": {
                |      "safeId": "XE00001234567890",
                |      "nino": "AA123456A",
                |      "mtdId": "XNIT00000068707",
                |      "yearOfMigration": "2023",
                |      "propertyIncomeFlag": true,
                |      "businessData": [
                |        {
                |          "incomeSourceId": "XAIS12345678910",
                |          "accPeriodSDate": "2001-01-01",
                |          "accPeriodEDate": "2001-01-02"
                |        }
                |      ]
                |    }
                |  }
                |}
              """.stripMargin
            )
            .as[RetrieveBusinessDetailsDownstreamResponse]

          val expected = RetrieveBusinessDetailsDownstreamResponse(
            yearOfMigration = Some("2023"),
            businessData = Some(
              List(BusinessData(
                incomeSourceId = "XAIS12345678910",
                tradingName = None,
                accountingPeriodStartDate = "2001-01-01",
                accountingPeriodEndDate = "2001-01-02",
                firstAccountingPeriodStartDate = None,
                firstAccountingPeriodEndDate = None,
                latencyDetails = None,
                tradingStartDate = None,
                cessationDate = None,
                businessAddressDetails = None,
                quarterTypeElection = None
              ))
            ),
            propertyData = None
          )

          result shouldBe expected
        }

        "only property data is supplied" in {
          val result = Json
            .parse(
              """
                |{
                |  "success": {
                |    "processingDate": "2023-07-05T09:16:58Z",
                |    "taxPayerDisplayResponse": {
                |      "safeId": "XE00001234567890",
                |      "nino": "AA123456A",
                |      "mtdId": "XNIT00000068707",
                |      "yearOfMigration": "2023",
                |      "propertyIncomeFlag": true,
                |      "propertyData": [
                |        {
                |          "incomeSourceId": "X0IS123456789012",
                |          "accPeriodSDate": "2019-04-06",
                |          "accPeriodEDate": "2020-04-05"
                |        }
                |      ]
                |    }
                |  }
                |}
              """.stripMargin
            )
            .as[RetrieveBusinessDetailsDownstreamResponse]

          val expected = RetrieveBusinessDetailsDownstreamResponse(
            yearOfMigration = Some("2023"),
            businessData = None,
            propertyData = Some(
              List(PropertyData(
                incomeSourceType = None,
                incomeSourceId = "X0IS123456789012",
                accountingPeriodStartDate = "2019-04-06",
                accountingPeriodEndDate = "2020-04-05",
                firstAccountingPeriodStartDate = None,
                firstAccountingPeriodEndDate = None,
                latencyDetails = None,
                tradingStartDate = None,
                cessationDate = None,
                quarterTypeElection = None
              ))
            )
          )

          result shouldBe expected
        }
      }
    }
  }

}
