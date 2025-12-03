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

import play.api.libs.json.{Reads, Writes}
import utils.enums.Enums

enum QuarterlyPeriodType(val asDownstream: String, val asHipDownstream: String) {
  case `standard` extends QuarterlyPeriodType("Standard", "STANDARD")
  case `calendar` extends QuarterlyPeriodType("Calendar", "CALENDAR")
}

object QuarterlyPeriodType {
  given Writes[QuarterlyPeriodType]                        = implicitly[Writes[String]].contramap(_.asDownstream)
  given Reads[QuarterlyPeriodType]                         = Enums.reads(values)
  val parser: PartialFunction[String, QuarterlyPeriodType] = Enums.parser(values)
}
