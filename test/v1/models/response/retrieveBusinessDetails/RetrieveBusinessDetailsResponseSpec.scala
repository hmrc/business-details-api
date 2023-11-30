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

package v1.models.response.retrieveBusinessDetails

import api.hateoas.Link
import api.hateoas.Method.GET
import api.models.domain.{AccountingType, TaxYear, TypeOfBusiness}
import config.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.response.retrieveBusinessDetails.downstream.{LatencyDetails, LatencyIndicator}

class RetrieveBusinessDetailsResponseSpec extends UnitSpec with MockAppConfig {

  "writes" should {
    "output JSON as per spec" in {
      Json.toJson(RetrieveBusinessDetailsResponse(
        businessId = "businessId",
        typeOfBusiness = TypeOfBusiness.`self-employment`,
        tradingName = Some("tradingName"),
        accountingPeriods = Seq(AccountingPeriod("2001-01-01", "2001-01-02")),
        accountingType = AccountingType.ACCRUALS,
        commencementDate = Some("2001-01-01"),
        cessationDate = Some("2010-01-01"),
        businessAddressLineOne = Some("line1"),
        businessAddressLineTwo = Some("line2"),
        businessAddressLineThree = Some("line3"),
        businessAddressLineFour = Some("line4"),
        businessAddressPostcode = Some("postCode"),
        businessAddressCountryCode = Some("country"),
        firstAccountingPeriodStartDate = Some("2018-04-06"),
        firstAccountingPeriodEndDate = Some("2018-12-12"),
        latencyDetails = Some(LatencyDetails(
          latencyEndDate = "2018-12-12",
          taxYear1 = TaxYear.fromDownstream("2018"),
          latencyIndicator1 = LatencyIndicator.Annual,
          taxYear2 = TaxYear.fromDownstream("2019"),
          latencyIndicator2 = LatencyIndicator.Quarterly
        )),
        yearOfMigration = Some("2023")
      )) shouldBe Json.parse(
        s"""
           |{
           |   "businessId": "businessId",
           |   "typeOfBusiness": "self-employment",
           |   "tradingName": "tradingName",
           |   "accountingPeriods": [
           |     {
           |       "start": "2001-01-01",
           |       "end": "2001-01-02"
           |     }
           |   ],
           |   "accountingType": "ACCRUALS",
           |   "commencementDate": "2001-01-01",
           |   "cessationDate": "2010-01-01",
           |   "businessAddressLineOne": "line1",
           |   "businessAddressLineTwo": "line2",
           |   "businessAddressLineThree": "line3",
           |   "businessAddressLineFour": "line4",
           |   "businessAddressPostcode": "postCode",
           |   "businessAddressCountryCode": "country",
           |   "firstAccountingPeriodStartDate": "2018-04-06",
           |   "firstAccountingPeriodEndDate": "2018-12-12",
           |   "latencyDetails": {
           |     "latencyEndDate": "2018-12-12",
           |     "taxYear1": "2017-18",
           |     "latencyIndicator1": "A",
           |     "taxYear2": "2018-19",
           |     "latencyIndicator2": "Q"
           |   },
           |   "yearOfMigration": "2023"
           |}
           |""".stripMargin
      )
    }
  }

  "LinksFactory" should {
    "expose the correct links" when {
      "called" in {
        val nino = "mynino"
        val businessId = "myid"
        MockedAppConfig.apiGatewayContext.returns("individuals/business/details").anyNumberOfTimes()
        RetrieveBusinessDetailsResponse.RetrieveBusinessDetailsLinksFactory
          .links(mockAppConfig, RetrieveBusinessDetailsHateoasData(nino, businessId)) shouldBe Seq(
          Link(s"/individuals/business/details/$nino/$businessId", GET, "self")
        )

      }
    }
  }

}
