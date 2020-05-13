/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.domain.{AccountingType, TypeOfBusiness}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse, RetrieveBusinessDetailsResponseList}

class RetrieveBusinessDetailsResponseListSpec extends UnitSpec {

  "reads" should{
    "read from json" when {
      "a json is supplied with a single business" in {
        val responseBody: RetrieveBusinessDetailsResponseList = RetrieveBusinessDetailsResponseList(Seq(RetrieveBusinessDetailsResponse(
          "XAIS12345678910",
          TypeOfBusiness.`self-employment`,
          Some("Aardvark Window Cleaning Services"),
          Some(Seq(AccountingPeriod("2018-04-06", "2019-04-05"))),
          Some(AccountingType.`ACCRUALS`),
          Some("2016-09-24"),
          Some("2020-03-24"),
          Some("6 Harpic Drive"),
          Some("Domestos Wood"),
          Some("ToiletDucktown"),
          Some("CIFSHIRE"),
          Some("SW4F 3GA"),
          Some("GB")
        )))
        val desJson: JsValue = Json.parse(
          """
            |{
            |   "retrieveBusinessDetailsResponse": [
            |      {
            |   "businessId": "XAIS12345678910",
            |   "typeOfBusiness": "self-employment",
            |   "tradingName": "Aardvark Window Cleaning Services",
            |   "accountingPeriods": [{
            |      "start": "2018-04-06",
            |      "end": "2019-04-05"
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
            |   "businessAddressCountryCode": "GB"
            |      }
            |   ]
            |}
            |""".stripMargin
        )
        responseBody shouldBe desJson.as[RetrieveBusinessDetailsResponseList]
      }
      "a json is supplied with multiple business" in {
        val responseBody: RetrieveBusinessDetailsResponseList = RetrieveBusinessDetailsResponseList(Seq(RetrieveBusinessDetailsResponse(
          "XAIS12345678910",
          TypeOfBusiness.`self-employment`,
          Some("Aardvark Window Cleaning Services"),
          Some(Seq(AccountingPeriod("2018-04-06", "2019-04-05"))),
          Some(AccountingType.`ACCRUALS`),
          Some("2016-09-24"),
          Some("2020-03-24"),
          Some("6 Harpic Drive"),
          Some("Domestos Wood"),
          Some("ToiletDucktown"),
          Some("CIFSHIRE"),
          Some("SW4F 3GA"),
          Some("GB")
        ),
          RetrieveBusinessDetailsResponse(
          "XAIS0987654321",
          TypeOfBusiness.`self-employment`,
          Some("Aardvark Window Cleaning Services"),
          Some(Seq(AccountingPeriod("2018-04-06", "2019-04-05"))),
          Some(AccountingType.`ACCRUALS`),
          Some("2016-09-24"),
          Some("2020-03-24"),
          Some("6 Harpic Drive"),
          Some("Domestos Wood"),
          Some("ToiletDucktown"),
          Some("CIFSHIRE"),
          Some("SW4F 3GA"),
          Some("GB")
        )))
        val desJson: JsValue = Json.parse(
          """
            |{
            |   "retrieveBusinessDetailsResponse": [
            |      {
            |   "businessId": "XAIS12345678910",
            |   "typeOfBusiness": "self-employment",
            |   "tradingName": "Aardvark Window Cleaning Services",
            |   "accountingPeriods": [{
            |      "start": "2018-04-06",
            |      "end": "2019-04-05"
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
            |   "businessAddressCountryCode": "GB"
            |  },
            |  {
            |   "businessId": "XAIS0987654321",
            |   "typeOfBusiness": "self-employment",
            |   "tradingName": "Aardvark Window Cleaning Services",
            |   "accountingPeriods": [{
            |      "start": "2018-04-06",
            |      "end": "2019-04-05"
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
            |   "businessAddressCountryCode": "GB"
            |      }
            |   ]
            |}
            |""".stripMargin
        )
        responseBody shouldBe desJson.as[RetrieveBusinessDetailsResponseList]
      }
    }
  }

}
