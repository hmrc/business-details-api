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

package v1.models.response.retrieveBusinessDetails.downstream

import play.api.libs.json.{Json, JsPath, OWrites, Reads}

case class RetrieveBusinessDetailsDownstreamResponse(businessDetails: Seq[BusinessDetails])

object RetrieveBusinessDetailsDownstreamResponse {

  implicit val writes: OWrites[RetrieveBusinessDetailsDownstreamResponse] = Json.writes[RetrieveBusinessDetailsDownstreamResponse]

  implicit val reads: Reads[RetrieveBusinessDetailsDownstreamResponse] = {
    implicit val businessDetailsReads: Reads[Seq[BusinessDetails]] = BusinessDetails.readsSeqBusinessData
    implicit val propertyDetailsReads: Reads[Seq[BusinessDetails]] = BusinessDetails.readsSeqPropertyData

    val businessDataReads: Reads[Seq[BusinessDetails]] =
      (JsPath \ "businessData").readNullable[Seq[BusinessDetails]](businessDetailsReads).map(_.getOrElse(Nil))
    val propertyDataReads: Reads[Seq[BusinessDetails]] =
      (JsPath \ "propertyData").readNullable[Seq[BusinessDetails]](propertyDetailsReads).map(_.getOrElse(Nil))

    for {
      businessData <- businessDataReads
      propertyData <- propertyDataReads
    } yield {
      RetrieveBusinessDetailsDownstreamResponse(businessData ++ propertyData)
    }
  }

}
