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

package v1.models.response.downstream.retrieveBusinessDetails

import play.api.libs.json._
import support.UnitSpec
import QuarterReportingType.{CALENDAR, STANDARD}
import QuarterReportingType._

class QuarterReportingTypeSpec extends UnitSpec {

  private val allQuarterReportingTypes: List[(String, QuarterReportingType, String)] = List(
    ("STANDARD", STANDARD, "standard"),
    ("CALENDAR", CALENDAR, "calendar")
  )

  "QuarterReportingType" should {
    allQuarterReportingTypes.foreach { case (asDownstream, parsed, asMtd) =>
      s"reads $asDownstream to $parsed correctly" in {
        val result = JsString(asDownstream).as[QuarterReportingType]

        result shouldBe parsed
      }

      s"writes $parsed to $asMtd correctly" in {
        Json.toJson(parsed) shouldBe JsString(asMtd)
      }
    }
  }

}
