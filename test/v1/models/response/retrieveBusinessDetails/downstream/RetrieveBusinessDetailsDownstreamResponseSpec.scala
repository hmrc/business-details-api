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

import config.MockFeatureSwitches
import play.api.libs.json.{JsValue, Json, Reads}
import support.UnitSpec

class RetrieveBusinessDetailsDownstreamResponseSpec extends UnitSpec with MockFeatureSwitches {

  "reads" should {
    "read the response from DES" when {
      "only business data is supplied" in {
        Json.parse(
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
            |    "accountingPeriodEndDate": "2001-01-02"
            |  }]
            |}
            |""".stripMargin
        ).as[RetrieveBusinessDetailsDownstreamResponse] shouldBe RetrieveBusinessDetailsDownstreamResponse(
          yearOfMigration = Some("2023"),
          businessData = Some(Seq(
            BusinessData(
              incomeSourceId = "XAIS12345678910",
              tradingName = None,
              accountingPeriodStartDate = "2001-01-01",
              accountingPeriodEndDate = "2001-01-02",
              firstAccountingPeriodStartDate = None,
              firstAccountingPeriodEndDate = None,
              latencyDetails = None,
              cashOrAccruals = None,
              tradingStartDate = None,
              cessationDate = None,
              businessAddressDetails = None))),
          propertyData = None)
      }

      "only property data is supplied" in {
        Json.parse(
          """
            |{
            |  "safeId": "XE00001234567890",
            |  "nino": "AA123456A",
            |  "mtdbsa": "123456789012345",
            |  "propertyIncome": true,
            |  "yearOfMigration": "2023",
            |  "propertyData": [{
            |    "incomeSourceId": "X0IS123456789012",
            |    "accountingPeriodStartDate": "2019-04-06",
            |    "accountingPeriodEndDate": "2020-04-05"
            |  }]
            |}
            |""".stripMargin
        ).as[RetrieveBusinessDetailsDownstreamResponse] shouldBe RetrieveBusinessDetailsDownstreamResponse(
          yearOfMigration = Some("2023"),
          businessData = None,
          propertyData = Some(Seq(PropertyData(
            incomeSourceType = None,
            incomeSourceId = "X0IS123456789012",
            accountingPeriodStartDate = "2019-04-06",
            accountingPeriodEndDate = "2020-04-05",
            firstAccountingPeriodStartDate = None,
            firstAccountingPeriodEndDate = None,
            latencyDetails = None,
            cashOrAccruals = None,
            tradingStartDate = None,
            cessationDate = None))))
      }
    }

    "read the response from IFS" when {
      "only business data is supplied" in {

        Json.parse(
          """
            |{
            |  "processingDate": "2023-07-05T09:16:58.655Z",
            |  "taxPayerDisplayResponse": {
            |    "safeId": "XE00001234567890",
            |    "nino": "AA123456A",
            |    "mtdbsa": "123456789012345",
            |    "yearOfMigration": "2023",
            |    "propertyIncome": false,
            |    "businessData": [{
            |      "incomeSourceId": "XAIS12345678910",
            |      "accountingPeriodStartDate": "2001-01-01",
            |      "accountingPeriodEndDate": "2001-01-02"
            |    }]
            |  }
            |}
            |""".stripMargin
        ).as[RetrieveBusinessDetailsDownstreamResponse] shouldBe RetrieveBusinessDetailsDownstreamResponse(
          yearOfMigration = Some("2023"),
          businessData = Some(Seq(
            BusinessData(
              incomeSourceId = "XAIS12345678910",
              tradingName = None,
              accountingPeriodStartDate = "2001-01-01",
              accountingPeriodEndDate = "2001-01-02",
              firstAccountingPeriodStartDate = None,
              firstAccountingPeriodEndDate = None,
              latencyDetails = None,
              cashOrAccruals = None,
              tradingStartDate = None,
              cessationDate = None,
              businessAddressDetails = None))),
          propertyData = None)
      }

      "only property data is supplied" in {
        Json.parse(
          """
            |{
            |  "processingDate": "2023-07-05T09:16:58.655Z",
            |  "taxPayerDisplayResponse": {
            |    "safeId": "XE00001234567890",
            |    "nino": "AA123456A",
            |    "mtdbsa": "123456789012345",
            |    "propertyIncome": true,
            |    "yearOfMigration": "2023",
            |    "propertyData": [{
            |      "incomeSourceId": "X0IS123456789012",
            |      "accountingPeriodStartDate": "2019-04-06",
            |      "accountingPeriodEndDate": "2020-04-05"
            |    }]
            |  }
            |}
            |""".stripMargin
        ).as[RetrieveBusinessDetailsDownstreamResponse] shouldBe RetrieveBusinessDetailsDownstreamResponse(
          yearOfMigration = Some("2023"),
          businessData = None,
          propertyData = Some(Seq(PropertyData(
            incomeSourceType = None,
            incomeSourceId = "X0IS123456789012",
            accountingPeriodStartDate = "2019-04-06",
            accountingPeriodEndDate = "2020-04-05",
            firstAccountingPeriodStartDate = None,
            firstAccountingPeriodEndDate = None,
            latencyDetails = None,
            cashOrAccruals = None,
            tradingStartDate = None,
            cessationDate = None))))
      }
    }
  }

  trait Test {
    val downstreamJson: JsValue
    val responseBody: RetrieveBusinessDetailsDownstreamResponse
    implicit val responseReads: Reads[RetrieveBusinessDetailsDownstreamResponse]
  }

}
