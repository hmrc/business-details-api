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

package v2.createAmendQuarterlyPeriodType.def1.model.request

import config.{AppConfig, FeatureSwitches}
import play.api.libs.json.*
import utils.EmptinessChecker
import utils.EmptinessChecker.field
import v2.createAmendQuarterlyPeriodType.model.request.CreateAmendQuarterlyPeriodTypeRequestBody

case class Def1_CreateAmendQuarterlyPeriodTypeRequestBody(quarterlyPeriodType: QuarterlyPeriodType) extends CreateAmendQuarterlyPeriodTypeRequestBody

object Def1_CreateAmendQuarterlyPeriodTypeRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def1_CreateAmendQuarterlyPeriodTypeRequestBody] = EmptinessChecker.use { o =>
    List(
      field("accountingType", o.quarterlyPeriodType.toString)
    )
  }

  implicit val reads: Reads[Def1_CreateAmendQuarterlyPeriodTypeRequestBody] = Json.reads[Def1_CreateAmendQuarterlyPeriodTypeRequestBody]

  implicit def writes(implicit appConfig: AppConfig): OWrites[Def1_CreateAmendQuarterlyPeriodTypeRequestBody] =
    (body: Def1_CreateAmendQuarterlyPeriodTypeRequestBody) => {
      if (FeatureSwitches(appConfig).isEnabled("ifs_hip_migration_2089")) {
        Json.obj("quarterReportingType" -> body.quarterlyPeriodType.asHipDownstream)
      } else {
        Json.obj("QRT" -> body.quarterlyPeriodType.asDownstream)
      }
    }

}
