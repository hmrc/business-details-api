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
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Business(typeOfBusiness: TypeOfBusiness, businessId: String, tradingName: Option[String])

object Business {

  implicit val writes: OWrites[Business] = Json.writes[Business]

  val readsBusinessData: Reads[Business] = (
    Reads.pure(TypeOfBusiness.`self-employment`) and
      (JsPath \ "incomeSourceId").read[String] and
      (JsPath \ "tradingName").readNullable[String]
  )(Business.apply _)

  val readsSeqBusinessData: Reads[Seq[Business]] = Reads.traversableReads[Seq, Business](implicitly, readsBusinessData)

  val readsPropertyData: Reads[Business] = (
    (JsPath \ "incomeSourceType").readNullable[TypeOfBusiness].map(_.getOrElse(TypeOfBusiness.`property-unspecified`)) and
      (JsPath \ "incomeSourceId").read[String] and
      Reads.pure(None)
  )(Business.apply _)

  val readsSeqPropertyData: Reads[Seq[Business]] = Reads.traversableReads[Seq, Business](implicitly, readsPropertyData)
}
