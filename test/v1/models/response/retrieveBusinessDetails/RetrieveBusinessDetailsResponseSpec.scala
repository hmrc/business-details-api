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
import config.{MockAppConfig, MockFeatureSwitches}
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.response.retrieveBusinessDetails.downstream._

class RetrieveBusinessDetailsResponseSpec extends UnitSpec with MockAppConfig with MockFeatureSwitches {

  "writes" should {
    "output JSON as per spec" in {
      Json.toJson(RetrieveBusinessDetailsResponse(
        businessId = "businessId",
        typeOfBusiness = TypeOfBusiness.`self-employment`,
        tradingName = Some("tradingName"),
        accountingPeriods = Seq(AccountingPeriod("2001-01-01", "2001-01-02")),
        accountingType = Some(AccountingType.ACCRUALS),
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

  "creating from response" when {
    val latencyDetails = Some(LatencyDetails("2018-12-12", TaxYear.fromDownstream("2018"), LatencyIndicator.Annual, TaxYear.fromDownstream("2019"), LatencyIndicator.Quarterly))
    val yearOfMigration = Some("migrationYear")

    "from property data" must {
      behave like convert(Some(TypeOfBusiness.`foreign-property`), TypeOfBusiness.`foreign-property`)
      behave like convert(Some(TypeOfBusiness.`uk-property`), TypeOfBusiness.`uk-property`)
      behave like convert(None, TypeOfBusiness.`property-unspecified`)

      def propertyData(typeOfBusiness: Option[TypeOfBusiness] = None, cashOrAccruals: Option[AccountingType] = Some(AccountingType.ACCRUALS)) =
        PropertyData(incomeSourceType = typeOfBusiness,
          incomeSourceId = "businessId",
          accountingPeriodStartDate = "accStartDate",
          accountingPeriodEndDate = "accEndDate",
          firstAccountingPeriodStartDate = Some("firstStartDate"),
          firstAccountingPeriodEndDate = Some("firstEndDate"),
          latencyDetails = latencyDetails,
          cashOrAccruals = cashOrAccruals,
          tradingStartDate = Some("tradingStartDate"),
          cessationDate = Some("cessationDate")
        )

      def propertyResponse(expectedTypeOfBusiness: TypeOfBusiness = TypeOfBusiness.`property-unspecified`, cashOrAccruals: Option[AccountingType] = Some(AccountingType.ACCRUALS)) = {
        RetrieveBusinessDetailsResponse(
          businessId = "businessId",
          typeOfBusiness = expectedTypeOfBusiness,
          tradingName = None,
          accountingPeriods = Seq(AccountingPeriod("accStartDate", "accEndDate")),
          accountingType = cashOrAccruals,
          commencementDate = Some("tradingStartDate"),
          cessationDate = Some("cessationDate"),
          businessAddressLineOne = None,
          businessAddressLineTwo = None,
          businessAddressLineThree = None,
          businessAddressLineFour = None,
          businessAddressPostcode = None,
          businessAddressCountryCode = None,
          firstAccountingPeriodStartDate = Some("firstStartDate"),
          firstAccountingPeriodEndDate = Some("firstEndDate"),
          latencyDetails = latencyDetails,
          yearOfMigration = yearOfMigration
        )
      }

      def convert(typeOfBusiness: Option[TypeOfBusiness], expectedTypeOfBusiness: TypeOfBusiness): Unit =
        s"find and convert to MTD for $typeOfBusiness" in {
          RetrieveBusinessDetailsResponse.fromPropertyData(propertyData(typeOfBusiness), yearOfMigration) shouldBe
            propertyResponse(expectedTypeOfBusiness)
        }

      "default accountingType to cash for IFS" in {
        MockFeatureSwitches.isIfsEnabled returns true
        RetrieveBusinessDetailsResponse.fromPropertyData(propertyData(cashOrAccruals = None), yearOfMigration) shouldBe
          propertyResponse(cashOrAccruals = Some(AccountingType.CASH))
      }

      "not default accountingType for DES" in {
        MockFeatureSwitches.isIfsEnabled returns false
        RetrieveBusinessDetailsResponse.fromPropertyData(propertyData(cashOrAccruals = None), yearOfMigration) shouldBe
          propertyResponse(cashOrAccruals = None)
      }
    }

    "from business data" must {
      val businessAddressDetails = Some(BusinessAddressDetails("line1", Some("line2"), Some("line3"), Some("line4"), Some("postcode"), "countryCode"))

      def businessData(businessAddressDetails: Option[BusinessAddressDetails] = businessAddressDetails, cashOrAccruals: Option[AccountingType] = Some(AccountingType.ACCRUALS)) =
        BusinessData(
          incomeSourceId = "businessId",
          accountingPeriodStartDate = "accStartDate",
          accountingPeriodEndDate = "accEndDate",
          tradingName = Some("tradingName"),
          businessAddressDetails = businessAddressDetails,
          firstAccountingPeriodStartDate = Some("firstStartDate"),
          firstAccountingPeriodEndDate = Some("firstEndDate"),
          latencyDetails = latencyDetails,
          cashOrAccruals = cashOrAccruals,
          tradingStartDate = Some("tradingStartDate"),
          cessationDate = Some("cessationDate")
        )

      def businessResponse(cashOrAccruals: Option[AccountingType] = Some(AccountingType.ACCRUALS)) = {
        RetrieveBusinessDetailsResponse(
          businessId = "businessId",
          typeOfBusiness = TypeOfBusiness.`self-employment`,
          tradingName = Some("tradingName"),
          accountingPeriods = Seq(AccountingPeriod("accStartDate", "accEndDate")),
          accountingType = cashOrAccruals,
          commencementDate = Some("tradingStartDate"),
          cessationDate = Some("cessationDate"),
          businessAddressLineOne = Some("line1"),
          businessAddressLineTwo = Some("line2"),
          businessAddressLineThree = Some("line3"),
          businessAddressLineFour = Some("line4"),
          businessAddressPostcode = Some("postcode"),
          businessAddressCountryCode = Some("countryCode"),
          firstAccountingPeriodStartDate = Some("firstStartDate"),
          firstAccountingPeriodEndDate = Some("firstEndDate"),
          latencyDetails = latencyDetails,
          yearOfMigration = yearOfMigration
        )
      }

      "find and convert to MTD" in {
        RetrieveBusinessDetailsResponse.fromBusinessData(businessData(businessAddressDetails), yearOfMigration) shouldBe
          businessResponse()
      }

      "for only mandatory address fields" in {
        val businessAddressDetails = Some(BusinessAddressDetails("line1", None, None, None, None, "countryCode"))
        RetrieveBusinessDetailsResponse.fromBusinessData(businessData(businessAddressDetails), yearOfMigration) shouldBe
          RetrieveBusinessDetailsResponse(
            businessId = "businessId",
            typeOfBusiness = TypeOfBusiness.`self-employment`,
            tradingName = Some("tradingName"),
            accountingPeriods = Seq(AccountingPeriod("accStartDate", "accEndDate")),
            accountingType = Some(AccountingType.ACCRUALS),
            commencementDate = Some("tradingStartDate"),
            cessationDate = Some("cessationDate"),
            businessAddressLineOne = Some("line1"),
            businessAddressLineTwo = None,
            businessAddressLineThree = None,
            businessAddressLineFour = None,
            businessAddressPostcode = None,
            businessAddressCountryCode = Some("countryCode"),
            firstAccountingPeriodStartDate = Some("firstStartDate"),
            firstAccountingPeriodEndDate = Some("firstEndDate"),
            latencyDetails = latencyDetails,
            yearOfMigration = yearOfMigration
          )
      }

      "for no business address" in {
        RetrieveBusinessDetailsResponse.fromBusinessData(businessData(businessAddressDetails = None), yearOfMigration) shouldBe
          RetrieveBusinessDetailsResponse(
            businessId = "businessId",
            typeOfBusiness = TypeOfBusiness.`self-employment`,
            tradingName = Some("tradingName"),
            accountingPeriods = Seq(AccountingPeriod("accStartDate", "accEndDate")),
            accountingType = Some(AccountingType.ACCRUALS),
            commencementDate = Some("tradingStartDate"),
            cessationDate = Some("cessationDate"),
            businessAddressLineOne = None,
            businessAddressLineTwo = None,
            businessAddressLineThree = None,
            businessAddressLineFour = None,
            businessAddressPostcode = None,
            businessAddressCountryCode = None,
            firstAccountingPeriodStartDate = Some("firstStartDate"),
            firstAccountingPeriodEndDate = Some("firstEndDate"),
            latencyDetails = latencyDetails,
            yearOfMigration = yearOfMigration
          )
      }

      "default accountingType to cash for IFS" in {
        MockFeatureSwitches.isIfsEnabled returns true

        RetrieveBusinessDetailsResponse.fromBusinessData(businessData(cashOrAccruals = None), yearOfMigration) shouldBe
          businessResponse(cashOrAccruals = Some(AccountingType.CASH))
      }

      "not default accountingType for DES" in {
        MockFeatureSwitches.isIfsEnabled returns false

        RetrieveBusinessDetailsResponse.fromBusinessData(businessData(cashOrAccruals = None), yearOfMigration) shouldBe
          businessResponse(cashOrAccruals = None)
      }
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
