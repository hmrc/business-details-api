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

import play.api.libs.json.Json
import support.UnitSpec

class Def1_CreateAmendQuarterlyPeriodTypeRequestBodySpec extends UnitSpec {

  private val validRequestBody = Json.parse("""
      |{
      | "quarterlyPeriodType": "standard"
      |}
      |""".stripMargin)

  private val downstreamRequestBody = Json.parse("""
      |{
      | "QRT": "Standard"
      |}
      |""".stripMargin)

  private val parsedRequestBody = Def1_CreateAmendQuarterlyPeriodTypeRequestBody(QuarterlyPeriodType.`standard`)

  "CreateAmendQuarterlyPeriodType" should {
    "read from vendor JSON" in {
      validRequestBody.as[Def1_CreateAmendQuarterlyPeriodTypeRequestBody] shouldBe parsedRequestBody
    }

    "write to downstream Json" in {
      Json.toJson(parsedRequestBody) shouldBe downstreamRequestBody
    }
  }

}
