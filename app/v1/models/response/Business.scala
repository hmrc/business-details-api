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

package v1.models.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.NestedJsonReads
import v1.models.domain.{IncomeSourceType, TypeOfBusiness}

case class Business(typeOfBusiness: TypeOfBusiness, businessId: String, tradingName: Option[String])

object Business extends NestedJsonReads {

  implicit val writes: OWrites[Business] = Json.writes[Business]
  implicit val reads: Reads[Business] = {
    val typeOfBusinessReads: Reads[TypeOfBusiness] =
      ((JsPath \ "businessData" \ "incomeSourceType").read[IncomeSourceType] or
        (JsPath \ "propertyData" \ "incomeSourceType").read[IncomeSourceType]).map(_.toTypeOfBusiness)

    val businessIdReads: Reads[String] =
      (JsPath \ "businessData" \ "incomeSourceId").read[String] or
        (JsPath \ "propertyData" \ "incomeSourceId").read[String]

    val tradingNameReads: Reads[Option[String]] =
      (JsPath \ "businessData" \ "tradingName").readNestedNullable[String]

    (typeOfBusinessReads and businessIdReads and tradingNameReads) (Business.apply _)
  }
}