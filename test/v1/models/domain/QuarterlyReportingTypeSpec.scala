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

package v1.models.domain

import play.api.libs.json._
import support.UnitSpec
import v1.models.domain.QuarterlyReportingType._

class QuarterlyReportingTypeSpec extends UnitSpec {

  private val allQuarterlyPeriodTypes: List[QuarterlyReportingType] = List(`standard`, `calendar`)

  "QuarterlyReportingType" should {
    allQuarterlyPeriodTypes.foreach { quarterlyReportingType =>
      s"read $quarterlyReportingType from JSON correctly" in {
        val json: JsValue                            = JsString(quarterlyReportingType.toString)
        val result: JsResult[QuarterlyReportingType] = Json.fromJson[QuarterlyReportingType](json)

        result shouldBe JsSuccess(quarterlyReportingType)
      }

      s"write $quarterlyReportingType to downstream format correctly" in {
        val downstreamJson: JsValue = JsString(quarterlyReportingType.asDownstream)
        Json.toJson(quarterlyReportingType) shouldBe downstreamJson
      }
    }

    "return a JsError" when {
      "reading an invalid QuarterlyPeriodType" in {
        val json: JsValue                            = JsString("Standard")
        val result: JsResult[QuarterlyReportingType] = Json.fromJson[QuarterlyReportingType](json)

        result shouldBe JsError(JsPath, JsonValidationError("error.expected.QuarterlyReportingType"))
      }
    }
  }

}
