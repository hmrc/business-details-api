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

import api.models.domain.Timestamp
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import v2.common.models.PeriodsOfAccountDates

case class RetrievePeriodsOfAccountResponse(submittedOn: Timestamp,
                                            periodsOfAccount: Boolean,
                                            periodsOfAccountDates: Option[Seq[PeriodsOfAccountDates]])

object RetrievePeriodsOfAccountResponse {

  implicit val reads: Reads[RetrievePeriodsOfAccountResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "periodsOfAccount").readWithDefault[Boolean](true) and
      (JsPath \ "periodsOfAccountDates").readNullable[Seq[PeriodsOfAccountDates]]
  )(RetrievePeriodsOfAccountResponse.apply _)

  implicit val writes: OWrites[RetrievePeriodsOfAccountResponse] = Json.writes[RetrievePeriodsOfAccountResponse]

}
