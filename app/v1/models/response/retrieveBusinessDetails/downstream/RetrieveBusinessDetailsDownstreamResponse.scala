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

import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveBusinessDetailsDownstreamResponse(businessDetails: Seq[BusinessDetails])

object RetrieveBusinessDetailsDownstreamResponse {

  private def getDownstreamPath(data: String)(implicit isIfsEnabled: Boolean): JsPath =
    if (isIfsEnabled) JsPath \ "taxPayerDisplayResponse" \ data else JsPath \ data

  val getReads: Boolean => Reads[RetrieveBusinessDetailsDownstreamResponse] = { implicit isIfsEnabled: Boolean =>
    val businessDataReads: Reads[Seq[BusinessDetails]] =
      getDownstreamPath("businessData").readNullable[Seq[BusinessDetails]](BusinessDetails.readsSeqBusinessData).map(_.getOrElse(Nil))
    val propertyDataReads: Reads[Seq[BusinessDetails]] =
      getDownstreamPath("propertyData").readNullable[Seq[BusinessDetails]](BusinessDetails.readsSeqPropertyData).map(_.getOrElse(Nil))
    val yearOfMigrationReads: Reads[Option[String]] =
      getDownstreamPath("yearOfMigration").readNullable[String]

    for {
      businessData    <- businessDataReads
      propertyData    <- propertyDataReads
      yearOfMigration <- yearOfMigrationReads
    } yield {
      val businessDataWithYearOfMigration: Seq[BusinessDetails] = businessData.map(_.copy(yearOfMigration = yearOfMigration))
      val propertyDataWithYearOfMigration: Seq[BusinessDetails] = propertyData.map(_.copy(yearOfMigration = yearOfMigration))
      RetrieveBusinessDetailsDownstreamResponse(businessDataWithYearOfMigration ++ propertyDataWithYearOfMigration)
    }
  }

  implicit val writes: OWrites[RetrieveBusinessDetailsDownstreamResponse] = Json.writes[RetrieveBusinessDetailsDownstreamResponse]
}
