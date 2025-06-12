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

package v2.createUpdatePeriodsOfAccount.request

import play.api.libs.json.{Json, OWrites, Reads}
import v2.common.models.PeriodsOfAccountDates

case class CreateUpdatePeriodsOfAccountRequestBody(periodsOfAccount: Boolean, periodsOfAccountDates: Option[Seq[PeriodsOfAccountDates]])

object CreateUpdatePeriodsOfAccountRequestBody {

  implicit val reads: Reads[CreateUpdatePeriodsOfAccountRequestBody] = Json.reads[CreateUpdatePeriodsOfAccountRequestBody]

  implicit val writes: OWrites[CreateUpdatePeriodsOfAccountRequestBody] = { body =>
    if (body.periodsOfAccount) {
      Json.obj("periodsOfAccountDates" -> body.periodsOfAccountDates)
    } else {
      Json.obj("periodsOfAccount" -> false)
    }
  }

}
