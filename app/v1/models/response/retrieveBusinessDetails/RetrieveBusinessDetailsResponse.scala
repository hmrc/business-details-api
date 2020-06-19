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

package v1.models.response.retrieveBusinessDetails

import config.AppConfig
import play.api.libs.json.{Json, OWrites, Reads}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.domain.TypeOfBusiness
import v1.models.domain.accountingType.AccountingType
import v1.models.hateoas.{HateoasData, Link}

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
                                           businessAddressCountryCode: Option[String])

object RetrieveBusinessDetailsResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveBusinessDetailsResponse] = Json.writes[RetrieveBusinessDetailsResponse]
  implicit val reads: Reads[RetrieveBusinessDetailsResponse] = Json.reads[RetrieveBusinessDetailsResponse]

  implicit object RetrieveBusinessDetailsLinksFactory extends HateoasLinksFactory[RetrieveBusinessDetailsResponse, RetrieveBusinessDetailsHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveBusinessDetailsHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveBusinessDetails(appConfig, nino, businessId)
      )
    }
  }

}

case class RetrieveBusinessDetailsHateoasData(nino: String, businessId: String) extends HateoasData