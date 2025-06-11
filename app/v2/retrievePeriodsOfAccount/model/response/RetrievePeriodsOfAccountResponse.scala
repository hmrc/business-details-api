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

package v2.retrievePeriodsOfAccount.model.response

import play.api.libs.json._
import v2.common.models.PeriodsOfAccountDates

case class RetrievePeriodsOfAccountResponse(periodsOfAccount: Option[Boolean], periodsOfAccountDates: Option[Seq[PeriodsOfAccountDates]])

object RetrievePeriodsOfAccountResponse {

  implicit val reads: Reads[RetrievePeriodsOfAccountResponse] = Reads[RetrievePeriodsOfAccountResponse](js => {
    val poa   = (js \ "periodsOfAccount").asOpt[Boolean]
    val dates = (js \ "periodsOfAccountDates").asOpt[Seq[PeriodsOfAccountDates]]

    (poa, dates) match {
      case (None, Some(dates)) => JsSuccess(RetrievePeriodsOfAccountResponse(Some(true), Some(dates)))
      case (Some(false), None) => JsSuccess(RetrievePeriodsOfAccountResponse(Some(false), None))
      case _                   => JsError("incorrect body returned")
    }
  })

  implicit val writes: OWrites[RetrievePeriodsOfAccountResponse] = Json.writes[RetrievePeriodsOfAccountResponse]

}
