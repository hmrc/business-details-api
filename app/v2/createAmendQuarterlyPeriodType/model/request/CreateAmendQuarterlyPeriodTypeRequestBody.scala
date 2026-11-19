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

package v2.createAmendQuarterlyPeriodType.model.request

import config.AppConfig
import play.api.libs.json.{JsObject, Json, OWrites, Reads}
import utils.EmptinessChecker
import utils.EmptinessChecker.field

case class CreateAmendQuarterlyPeriodTypeRequestBody(quarterlyPeriodType: QuarterlyPeriodType)

object CreateAmendQuarterlyPeriodTypeRequestBody {

  implicit val emptinessChecker: EmptinessChecker[CreateAmendQuarterlyPeriodTypeRequestBody] = EmptinessChecker.use { o =>
    List(
      field("accountingType", o.quarterlyPeriodType.toString)
    )
  }

  implicit val reads: Reads[CreateAmendQuarterlyPeriodTypeRequestBody] = Json.reads[CreateAmendQuarterlyPeriodTypeRequestBody]

  implicit def writes(implicit appConfig: AppConfig): OWrites[CreateAmendQuarterlyPeriodTypeRequestBody] =
    (body: CreateAmendQuarterlyPeriodTypeRequestBody) => {
      Json.obj("quarterReportingType" -> body.quarterlyPeriodType.asHipDownstream)
    }

}
