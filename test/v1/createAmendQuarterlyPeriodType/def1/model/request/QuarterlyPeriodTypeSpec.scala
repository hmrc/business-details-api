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

package v1.createAmendQuarterlyPeriodType.def1.model.request

import play.api.libs.json._
import support.UnitSpec
import v1.createAmendQuarterlyPeriodType.def1.model.request.QuarterlyPeriodType._

class QuarterlyPeriodTypeSpec extends UnitSpec {

  private val allQuarterlyPeriodTypes: List[QuarterlyPeriodType] = List(`standard`, `calendar`)

  "QuarterlyPeriodType" should {
    allQuarterlyPeriodTypes.foreach { quarterlyPeriodType =>
      s"read $quarterlyPeriodType from JSON correctly" in {
        val json: JsValue                         = JsString(quarterlyPeriodType.toString)
        val result: JsResult[QuarterlyPeriodType] = Json.fromJson[QuarterlyPeriodType](json)

        result shouldBe JsSuccess(quarterlyPeriodType)
      }

      s"write $quarterlyPeriodType to ifs downstream format correctly" in {
        val downstreamJson: JsValue = JsString(quarterlyPeriodType.asDownstream)
        Json.toJson(quarterlyPeriodType) shouldBe downstreamJson
      }
    }

    "return a JsError" when {
      "reading an invalid QuarterlyPeriodType" in {
        val json: JsValue                         = JsString("Standard")
        val result: JsResult[QuarterlyPeriodType] = Json.fromJson[QuarterlyPeriodType](json)

        result shouldBe JsError(JsPath, JsonValidationError("error.expected.QuarterlyPeriodType"))
      }
    }
  }

}
