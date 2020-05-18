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

package v1.models.response.retrieveBusinessDetails.des

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class RetrieveBusinessDetailsDesResponse(businessDetails: Seq[BusinessDetails])

object RetrieveBusinessDetailsDesResponse {
  implicit val reads: Reads[RetrieveBusinessDetailsDesResponse] = (
    (JsPath \ "businessData").read[Seq[BusinessDetails]] or
      (JsPath \ "propertyData").read[Seq[BusinessDetails]]
    ).map(RetrieveBusinessDetailsDesResponse(_))
}
