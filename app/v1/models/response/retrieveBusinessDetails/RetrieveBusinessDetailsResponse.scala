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

import api.hateoas.{HateoasData, HateoasLinks, HateoasLinksFactory, Link}
import api.models.domain.{AccountingType, TypeOfBusiness}
import config.{AppConfig, FeatureSwitches}
import play.api.libs.json.{Json, OWrites}
import v1.models.response.retrieveBusinessDetails.downstream.{BusinessData, LatencyDetails, PropertyData, QuarterTypeElection}

case class RetrieveBusinessDetailsResponse(businessId: String,
                                           typeOfBusiness: TypeOfBusiness,
                                           tradingName: Option[String],
                                           accountingPeriods: Seq[AccountingPeriod],
                                           accountingType: Option[AccountingType],
                                           commencementDate: Option[String],
                                           cessationDate: Option[String],
                                           businessAddressLineOne: Option[String],
                                           businessAddressLineTwo: Option[String],
                                           businessAddressLineThree: Option[String],
                                           businessAddressLineFour: Option[String],
                                           businessAddressPostcode: Option[String],
                                           businessAddressCountryCode: Option[String],
                                           firstAccountingPeriodStartDate: Option[String],
                                           firstAccountingPeriodEndDate: Option[String],
                                           latencyDetails: Option[LatencyDetails],
                                           yearOfMigration: Option[String],
                                           quarterTypeChoice: Option[QuarterTypeElection])

object RetrieveBusinessDetailsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveBusinessDetailsResponse] = Json.writes[RetrieveBusinessDetailsResponse]

  implicit object RetrieveBusinessDetailsLinksFactory
      extends HateoasLinksFactory[RetrieveBusinessDetailsResponse, RetrieveBusinessDetailsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveBusinessDetailsHateoasData): Seq[Link] = {
      import data._
      List(
        retrieveBusinessDetails(appConfig, nino, businessId)
      )
    }

  }

  def fromBusinessData(businessData: BusinessData, yearOfMigration: Option[String])(implicit
      featureSwitches: FeatureSwitches): RetrieveBusinessDetailsResponse = {
    import businessData._

    RetrieveBusinessDetailsResponse(
      businessId = incomeSourceId,
      typeOfBusiness = TypeOfBusiness.`self-employment`,
      tradingName = tradingName,
      accountingPeriods = Seq(AccountingPeriod(accountingPeriodStartDate, accountingPeriodEndDate)),
      accountingType = cashOrAccruals.orElse(defaultAccountingType),
      commencementDate = tradingStartDate,
      cessationDate = cessationDate,
      businessAddressLineOne = businessAddressDetails.map(_.addressLine1),
      businessAddressLineTwo = businessAddressDetails.flatMap(_.addressLine2),
      businessAddressLineThree = businessAddressDetails.flatMap(_.addressLine3),
      businessAddressLineFour = businessAddressDetails.flatMap(_.addressLine4),
      businessAddressPostcode = businessAddressDetails.flatMap(_.postalCode),
      businessAddressCountryCode = businessAddressDetails.map(_.countryCode),
      firstAccountingPeriodStartDate: Option[String],
      firstAccountingPeriodEndDate: Option[String],
      latencyDetails: Option[LatencyDetails],
      yearOfMigration: Option[String],
      quarterTypeChoice = quarterTypeElection
    )
  }

  def fromPropertyData(propertyData: PropertyData, yearOfMigration: Option[String])(implicit
      featureSwitches: FeatureSwitches): RetrieveBusinessDetailsResponse = {
    import propertyData._

    RetrieveBusinessDetailsResponse(
      businessId = incomeSourceId,
      typeOfBusiness = incomeSourceType.getOrElse(TypeOfBusiness.`property-unspecified`),
      tradingName = None,
      accountingPeriods = Seq(AccountingPeriod(accountingPeriodStartDate, accountingPeriodEndDate)),
      accountingType = cashOrAccruals.orElse(defaultAccountingType),
      commencementDate = tradingStartDate,
      cessationDate = cessationDate,
      businessAddressLineOne = None,
      businessAddressLineTwo = None,
      businessAddressLineThree = None,
      businessAddressLineFour = None,
      businessAddressPostcode = None,
      businessAddressCountryCode = None,
      firstAccountingPeriodStartDate: Option[String],
      firstAccountingPeriodEndDate: Option[String],
      latencyDetails: Option[LatencyDetails],
      yearOfMigration: Option[String],
      quarterTypeChoice = quarterTypeElection
    )
  }

  private def defaultAccountingType(implicit featureSwitches: FeatureSwitches) = {
    if (featureSwitches.isIfsEnabled) Some(AccountingType.CASH) else None
  }

}

case class RetrieveBusinessDetailsHateoasData(nino: String, businessId: String) extends HateoasData
