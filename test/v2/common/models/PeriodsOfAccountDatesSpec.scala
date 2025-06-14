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

package v2.common.models

import play.api.libs.json.Json
import support.UnitSpec

class PeriodsOfAccountDatesSpec extends UnitSpec {

  private val validJson = Json.parse("""
      |{
      | "startDate": "2024-04-06",
      | "endDate": "2025-04-05"
      |}
      |""".stripMargin)

  private val parsedDates = PeriodsOfAccountDates("2024-04-06", "2025-04-05")

  "PeriodOfAccountsDates" should {
    "read from json" in {
      validJson.as[PeriodsOfAccountDates] shouldBe parsedDates
    }

    "write to json" in {
      Json.toJson(parsedDates) shouldBe validJson
    }

  }

}
