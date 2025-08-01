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
import play.api.libs.json.{JsObject, Json, OWrites}
import utils.JsonWritesUtil
import v2.createAmendQuarterlyPeriodType.def1.model.request.Def1_CreateAmendQuarterlyPeriodTypeRequestBody

trait CreateAmendQuarterlyPeriodTypeRequestBody

object CreateAmendQuarterlyPeriodTypeRequestBody extends JsonWritesUtil {

  implicit def writes(implicit appConfig: AppConfig): OWrites[CreateAmendQuarterlyPeriodTypeRequestBody] = writesFrom {
    case a: Def1_CreateAmendQuarterlyPeriodTypeRequestBody =>
    Json.toJson(a).as[JsObject]
  }

}
