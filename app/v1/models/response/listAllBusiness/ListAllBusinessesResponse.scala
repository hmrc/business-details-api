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

package v1.models.response.listAllBusiness

import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class ListAllBusinessesResponse(listOfBusinesses: Seq[Business])

object ListAllBusinessesResponse {
  implicit val reads: Reads[ListAllBusinessesResponse] = {
    val businessDataReads: Reads[Seq[Business]] =
      (JsPath \ "businessData").readNullable[Seq[Business]](Business.readsSeqBusinessData).map(_.getOrElse(Nil))
    val propertyDataReads: Reads[Seq[Business]] =
      (JsPath \ "propertyData").readNullable[Seq[Business]](Business.readsSeqPropertyData).map(_.getOrElse(Nil))

    for {
      businessData <- businessDataReads
      propertyData <- propertyDataReads
    } yield {
      ListAllBusinessesResponse(businessData ++ propertyData)
    }
  }

  implicit val writes: OWrites[ListAllBusinessesResponse] = Json.writes[ListAllBusinessesResponse]
}