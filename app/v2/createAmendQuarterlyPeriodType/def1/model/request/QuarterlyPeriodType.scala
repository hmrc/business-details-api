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

package v2.createAmendQuarterlyPeriodType.def1.model.request

import play.api.libs.json.{Reads, Writes}
import utils.enums.Enums

sealed trait QuarterlyPeriodType {
  val asDownstream: String
  val asHipDownstream: String
}

object QuarterlyPeriodType {

  implicit val writes: Writes[QuarterlyPeriodType] = implicitly[Writes[String]].contramap(_.asDownstream)

  implicit val reads: Reads[QuarterlyPeriodType] = Enums.reads[QuarterlyPeriodType]

  val parser: PartialFunction[String, QuarterlyPeriodType] = Enums.parser[QuarterlyPeriodType]

  case object `standard` extends QuarterlyPeriodType {
    val asDownstream: String = "Standard"
    val asHipDownstream: String = "STANDARD"
  }

  case object `calendar` extends QuarterlyPeriodType {
    val asDownstream: String = "Calendar"
    val asHipDownstream: String = "CALENDAR"
  }

}
