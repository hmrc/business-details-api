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
import api.models.domain.{AccountingType, TaxYear, TypeOfBusiness}
import config.AppConfig
import play.api.libs.json.{Json, OWrites, Reads}
import v1.models.response.retrieveBusinessDetails.downstream.LatencyDetails

case class RetrieveBusinessDetailsResponse(businessId: String,
                                           typeOfBusiness: TypeOfBusiness,
                                           tradingName: Option[String],
                                           accountingPeriods: Seq[AccountingPeriod],
                                           accountingType: AccountingType,
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
                                           yearOfMigration: Option[String]) {

  def addRetrieveAdditionalFields: RetrieveBusinessDetailsResponse = {
    val updatedResponse = this.copy(
      firstAccountingPeriodStartDate = if (firstAccountingPeriodStartDate.isEmpty) None else firstAccountingPeriodStartDate,
      firstAccountingPeriodEndDate = if (firstAccountingPeriodEndDate.isEmpty) None else firstAccountingPeriodEndDate,
      latencyDetails = if (latencyDetails.isEmpty) None else latencyDetails,
      yearOfMigration = if (yearOfMigration.isEmpty) None else yearOfMigration
    )
    updatedResponse
  }

  def reformatLatencyDetailsTaxYears: RetrieveBusinessDetailsResponse = {
    val updatedLatencyDetails = latencyDetails.map(details => details.copy(
      taxYear1 = TaxYear.fromDownstream(details.taxYear1).asMtd,
      taxYear2 = TaxYear.fromDownstream(details.taxYear2).asMtd
    ))
    copy(latencyDetails = updatedLatencyDetails)
  }

}

object RetrieveBusinessDetailsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveBusinessDetailsResponse] = Json.writes[RetrieveBusinessDetailsResponse]

  // FIXME this should not be here: we should only ever write RetrieveBusinessDetailsResponse, but a test depends on it ???
  implicit val reads: Reads[RetrieveBusinessDetailsResponse]    = Json.reads[RetrieveBusinessDetailsResponse]

  implicit object RetrieveBusinessDetailsLinksFactory
      extends HateoasLinksFactory[RetrieveBusinessDetailsResponse, RetrieveBusinessDetailsHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveBusinessDetailsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveBusinessDetails(appConfig, nino, businessId)
      )
    }

  }

}

case class RetrieveBusinessDetailsHateoasData(nino: String, businessId: String) extends HateoasData
