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

package v1.retrieveBusinessDetails.model.response.downstream

import api.models.domain.TaxYear
import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class LatencyDetails(latencyEndDate: String,
                          taxYear1: TaxYear,
                          latencyIndicator1: LatencyIndicator,
                          taxYear2: TaxYear,
                          latencyIndicator2: LatencyIndicator)

object LatencyDetails {
  implicit val writes: OWrites[LatencyDetails] = Json.writes[LatencyDetails]

  implicit val taxYearReads: Reads[TaxYear] = implicitly[Reads[String]].map(TaxYear.fromDownstream)

  implicit val reads: Reads[LatencyDetails] = (
    (JsPath \ "latencyEndDate").read[String] and
      (JsPath \ "taxYear1").read[TaxYear] and
      (JsPath \ "latencyIndicator1").read[LatencyIndicator] and
      (JsPath \ "taxYear2").read[TaxYear] and
      (JsPath \ "latencyIndicator2").read[LatencyIndicator]
  )(LatencyDetails.apply)

}
