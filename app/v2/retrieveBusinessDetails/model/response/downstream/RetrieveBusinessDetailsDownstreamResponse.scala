/*
 * Copyright 2025 HM Revenue & Customs
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

package v2.retrieveBusinessDetails.model.response.downstream

import play.api.libs.json.{JsPath, Json, Reads}

case class RetrieveBusinessDetailsDownstreamResponse(yearOfMigration: Option[String],
                                                     businessData: Option[Seq[BusinessData]],
                                                     propertyData: Option[Seq[PropertyData]])

object RetrieveBusinessDetailsDownstreamResponse {

  implicit val reads: Reads[RetrieveBusinessDetailsDownstreamResponse] = {
    val defaultReads: Reads[RetrieveBusinessDetailsDownstreamResponse] = Json.reads

    (JsPath \ "success" \ "taxPayerDisplayResponse")
      .read[RetrieveBusinessDetailsDownstreamResponse](defaultReads)
      .orElse((JsPath \ "taxPayerDisplayResponse").read[RetrieveBusinessDetailsDownstreamResponse](defaultReads))
      .orElse(defaultReads)
  }

}
