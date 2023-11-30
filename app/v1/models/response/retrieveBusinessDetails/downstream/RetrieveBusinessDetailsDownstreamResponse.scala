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

import config.FeatureSwitches
import play.api.libs.json.{JsPath, Reads}

case class RetrieveBusinessDetailsDownstreamResponse(businessDetails: Seq[BusinessDetails])

object RetrieveBusinessDetailsDownstreamResponse {

  def reads(implicit featureSwitches: FeatureSwitches): Reads[RetrieveBusinessDetailsDownstreamResponse] = {

    val readsSeqBusinessData: Reads[Seq[BusinessDetails]] =
      Reads.traversableReads[Seq, BusinessDetails](implicitly, BusinessDetails.readsBusinessData)

    val readsSeqPropertyData: Reads[Seq[BusinessDetails]] =
      Reads.traversableReads[Seq, BusinessDetails](implicitly, BusinessDetails.readsPropertyData)

    def getDownstreamPath(pathSegment: String): JsPath =
      if (featureSwitches.isIfsEnabled) JsPath \ "taxPayerDisplayResponse" \ pathSegment else JsPath \ pathSegment

    for {
      businessData <- getDownstreamPath("businessData").readNullable[Seq[BusinessDetails]](readsSeqBusinessData).map(_.getOrElse(Nil))
      propertyData <- getDownstreamPath("propertyData").readNullable[Seq[BusinessDetails]](readsSeqPropertyData).map(_.getOrElse(Nil))
      yearOfMigration <- getDownstreamPath("yearOfMigration").readNullable[String]
    } yield {
      val businessDataWithYearOfMigration: Seq[BusinessDetails] = businessData.map(_.copy(yearOfMigration = yearOfMigration))
      val propertyDataWithYearOfMigration: Seq[BusinessDetails] = propertyData.map(_.copy(yearOfMigration = yearOfMigration))
      RetrieveBusinessDetailsDownstreamResponse(businessDataWithYearOfMigration ++ propertyDataWithYearOfMigration)
    }
  }
}
