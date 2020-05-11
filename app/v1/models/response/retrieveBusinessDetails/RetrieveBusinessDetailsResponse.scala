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

import play.api.libs.json.{Json, OWrites, Reads}
import v1.models.domain.{AccountingType, TypeOfBusiness}

case class RetrieveBusinessDetailsResponse(businessId: String,
                                           typeOfBusiness: TypeOfBusiness,
                                           tradingName: String,
                                           accountingPeriods: Seq[AccountingPeriod],
                                           accountingType: AccountingType,
                                           commencementDate: String,
                                           cessationDate: String,
                                           businessAddressLineOne: String,
                                           businessAddressLineTwo: String,
                                           businessAddressLineThree: String,
                                           businessAddressLineFour: String,
                                           businessAddressPostcode: String,
                                           businessAddressCountryCode: CountryCode)

object RetrieveBusinessDetailsResponse {

  implicit val writes: OWrites[RetrieveBusinessDetailsResponse] = Json.writes[RetrieveBusinessDetailsResponse]
  implicit val reads: Reads[RetrieveBusinessDetailsResponse] = Json.reads[RetrieveBusinessDetailsResponse]

}