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

package v2.retrievePeriodsOfAccount.model.response

import play.api.libs.json.Json
import support.UnitSpec
import v2.common.models.PeriodsOfAccountDates

class RetrievePeriodsOfAccountResponseSpec extends UnitSpec {

  private val downstreamResponseWithDates = Json.parse("""
      |{
      |  "submittedOn": "2019-08-24T14:15:22Z",
      |  "periodsOfAccountDates": [
      |    {
      |      "startDate": "2024-04-06",
      |      "endDate": "2025-03-05"
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val downstreamResponseWithoutDates = Json.parse("""
      |{
      |  "submittedOn": "2019-08-24T14:15:22Z",
      |  "periodsOfAccount": false
      |}
      |""".stripMargin)

  private val vendorResponseWithDates = Json.parse("""
      |{
      |  "periodsOfAccount": true,
      |  "periodsOfAccountDates": [
      |    {
      |      "startDate": "2024-04-06",
      |      "endDate": "2025-03-05"
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val vendorResponseWithoutDates = Json.parse("""
      |{
      |  "periodsOfAccount": false
      |}
      |""".stripMargin)

  private val parsedResponseWithDates =
    RetrievePeriodsOfAccountResponse(periodsOfAccount = true, Some(Seq(PeriodsOfAccountDates("2024-04-06", "2025-03-05"))))

  private val parsedResponseWithoutDates = RetrievePeriodsOfAccountResponse(periodsOfAccount = false, None)

  "RetrievePeriodsOfAccountResponse" when {
    "There are periods of accounts dates" should {
      "read from json" in {
        downstreamResponseWithDates.as[RetrievePeriodsOfAccountResponse] shouldBe parsedResponseWithDates
      }

      "write to json" in {
        Json.toJson(parsedResponseWithDates) shouldBe vendorResponseWithDates
      }
    }

  }

  "There are no periods of accounts dates" should {
    "read from json" in {
      downstreamResponseWithoutDates.as[RetrievePeriodsOfAccountResponse] shouldBe parsedResponseWithoutDates
    }

    "write to json" in {
      Json.toJson(parsedResponseWithoutDates) shouldBe vendorResponseWithoutDates
    }
  }

}
