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

package v1.models.response.RetrieveBusinessDetails

import api.models.domain.TypeOfBusiness
import api.models.domain.accountingType.AccountingType
import api.models.hateoas.Method.GET
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import api.models.hateoas.Link
import v1.models.response.retrieveBusinessDetails.downstream.{LatencyDetails, LatencyIndicator}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsHateoasData, RetrieveBusinessDetailsResponse}

class RetrieveBusinessDetailsResponseSpec extends UnitSpec with MockAppConfig {

  "reads" should {
    "read from json" when {
      "A full json is supplied" in {

        val responseBody: RetrieveBusinessDetailsResponse = RetrieveBusinessDetailsResponse(
          "XAIS12345678910",
          TypeOfBusiness.`self-employment`,
          Some("Aardvark Window Cleaning Services"),
          Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
          Some(AccountingType.ACCRUALS),
          Some("2016-09-24"),
          Some("2020-03-24"),
          Some("6 Harpic Drive"),
          Some("Domestos Wood"),
          Some("ToiletDucktown"),
          Some("CIFSHIRE"),
          Some("SW4F 3GA"),
          Some("GB"),
          Some("2018-04-06"),
          Some("2018-12-12"),
          Some(LatencyDetails("2018-12-12", "2018", LatencyIndicator.Annual, "2019", LatencyIndicator.Quarterly)),
          Some("2023"),
        )

        val desJson: JsValue = Json.parse(
          """
            |{
            |   "businessId": "XAIS12345678910",
            |   "typeOfBusiness": "self-employment",
            |   "tradingName": "Aardvark Window Cleaning Services",
            |   "accountingPeriods": [{
            |      "accountingPeriodStartDate": "2018-04-06",
            |      "accountingPeriodEndDate": "2019-04-05"
            |      }
            |   ],
            |   "accountingType": "ACCRUALS",
            |   "commencementDate": "2016-09-24",
            |   "cessationDate": "2020-03-24",
            |   "businessAddressLineOne": "6 Harpic Drive",
            |   "businessAddressLineTwo": "Domestos Wood",
            |   "businessAddressLineThree": "ToiletDucktown",
            |   "businessAddressLineFour": "CIFSHIRE",
            |   "businessAddressPostcode": "SW4F 3GA",
            |   "businessAddressCountryCode": "GB","firstAccountingPeriodStartDate": "2018-04-06",
            |   "firstAccountingPeriodEndDate": "2018-12-12",
            |   "latencyDetails": {
            |     "latencyEndDate": "2018-12-12",
            |     "taxYear1": "2018",
            |     "latencyIndicator1": "A",
            |     "taxYear2": "2019",
            |     "latencyIndicator2": "Q"
            |   },
            |   "yearOfMigration": "2023"
            |}
            |""".stripMargin
        )

        responseBody shouldBe desJson.as[RetrieveBusinessDetailsResponse]
      }
      "A partial json is supplied with end missing" in {

        val responseBody: RetrieveBusinessDetailsResponse = RetrieveBusinessDetailsResponse(
          "XAIS12345678910",
          TypeOfBusiness.`self-employment`,
          Some("Aardvark Window Cleaning Services"),
          Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
          Some(AccountingType.ACCRUALS),
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
        )

        val desJson: JsValue = Json.parse(
          """
            |{
            |   "businessId": "XAIS12345678910",
            |   "typeOfBusiness": "self-employment",
            |   "tradingName": "Aardvark Window Cleaning Services",
            |   "accountingPeriods": [{
            |      "accountingPeriodStartDate": "2018-04-06",
            |      "accountingPeriodEndDate": "2019-04-05"
            |      }
            |   ],
            |   "accountingType": "ACCRUALS"
            |}
            |""".stripMargin
        )

        responseBody shouldBe desJson.as[RetrieveBusinessDetailsResponse]
      }
      "A partial json is supplied with middle missing" in {

        val responseBody: RetrieveBusinessDetailsResponse = RetrieveBusinessDetailsResponse(
          "XAIS12345678910",
          TypeOfBusiness.`self-employment`,
          Some("Aardvark Window Cleaning Services"),
          Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
          Some(AccountingType.ACCRUALS),
          Some("2016-09-24"),
          Some("2020-03-24"),
          None,
          None,
          None,
          None,
          None,
          Some("CM"),
          Some("2018-04-06"),
          Some("2018-12-12"),
          Some(LatencyDetails("2018-12-12", "2018", LatencyIndicator.Annual, "2019", LatencyIndicator.Quarterly)),
          Some("2023"),
        )

        val desJson: JsValue = Json.parse(
          """
            |{
            |   "businessId": "XAIS12345678910",
            |   "typeOfBusiness": "self-employment",
            |   "tradingName": "Aardvark Window Cleaning Services",
            |   "accountingPeriods": [{
            |      "accountingPeriodStartDate": "2018-04-06",
            |      "accountingPeriodEndDate": "2019-04-05"
            |      }
            |   ],
            |   "accountingType": "ACCRUALS",
            |   "commencementDate": "2016-09-24",
            |   "cessationDate": "2020-03-24",
            |   "businessAddressCountryCode": "CM",
            |   "firstAccountingPeriodStartDate": "2018-04-06",
            |   "firstAccountingPeriodEndDate": "2018-12-12",
            |   "latencyDetails": {
            |     "latencyEndDate": "2018-12-12",
            |     "taxYear1": "2018",
            |     "latencyIndicator1": "A",
            |     "taxYear2": "2019",
            |     "latencyIndicator2": "Q"
            |   },
            |   "yearOfMigration": "2023"
            |}
            |""".stripMargin
        )

        responseBody shouldBe desJson.as[RetrieveBusinessDetailsResponse]
      }
    }
  }

  "LinksFactory" should {
    "expose the correct links" when {
      "called" in {
        val nino       = "mynino"
        val businessId = "myid"
        MockAppConfig.apiGatewayContext.returns("individuals/business/details").anyNumberOfTimes()
        RetrieveBusinessDetailsResponse.RetrieveBusinessDetailsLinksFactory
          .links(mockAppConfig, RetrieveBusinessDetailsHateoasData(nino, businessId)) shouldBe Seq(
          Link(s"/individuals/business/details/$nino/$businessId", GET, "self")
        )

      }
    }
  }

}
