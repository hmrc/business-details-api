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

package v1.models.response.listAllBusinesses

import api.models.domain.TypeOfBusiness
import play.api.libs.json.{Json, OWrites}
import v1.models.response.downstream.retrieveBusinessDetails.{BusinessData, PropertyData}

case class Business(typeOfBusiness: TypeOfBusiness, businessId: String, tradingName: Option[String])

object Business {

  implicit val writes: OWrites[Business] = Json.writes[Business]

  def fromDownstreamBusiness(businessData: BusinessData): Business = {
    import businessData._
    Business(
      TypeOfBusiness.`self-employment`,
      businessId = incomeSourceId,
      tradingName
    )
  }

  def fromDownstreamProperty(propertyData: PropertyData): Business = {
    import propertyData._
    Business(
      propertyData.incomeSourceType.getOrElse(TypeOfBusiness.`property-unspecified`),
      businessId = incomeSourceId,
      tradingName = None
    )
  }

}
