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

package v2.createUpdatePeriodsOfAccount.model.request

import play.api.libs.json.Json
import support.UnitSpec
import v2.common.models.PeriodsOfAccountDates
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequestBody

class CreateUpdatePeriodsOfAccountRequestBodySpec extends UnitSpec {

  private val validRequestWithPOA = Json.parse("""
      |{
      |  "periodsOfAccount": true,
      |  "periodsOfAccountDates" : [
      |    {
      |      "startDate": "2024-04-06",
      |      "endDate": "2025-04-05"
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val validRequestWithoutPOA = Json.parse("""
      |{
      |  "periodsOfAccount": false
      |}
      |""".stripMargin)

  private val parsedRequestBodyPOA = CreateUpdatePeriodsOfAccountRequestBody(true, Some(Seq(PeriodsOfAccountDates("2024-04-06", "2025-04-05"))))

  private val parsedRequestBodyWithoutPOA = CreateUpdatePeriodsOfAccountRequestBody(false, None)

  "CreateUpdatePeriodsOfAccountRequestBody" when {
    "the request includes periods of account" should {
      "read from json" in {
        validRequestWithPOA.as[CreateUpdatePeriodsOfAccountRequestBody] shouldBe parsedRequestBodyPOA
      }
      "write to json" in {
        Json.toJson(parsedRequestBodyPOA) shouldBe validRequestWithPOA
      }
    }
    "the request does not contain periods of account" should {
      "read from json" in {
        validRequestWithoutPOA.as[CreateUpdatePeriodsOfAccountRequestBody] shouldBe parsedRequestBodyWithoutPOA
      }
      "write to json" in {
        Json.toJson(parsedRequestBodyWithoutPOA) shouldBe validRequestWithoutPOA
      }
    }
  }

}
