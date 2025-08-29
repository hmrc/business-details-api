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

package v2.retrieveLateAccountingDateRule.response

import api.models.domain.TaxYear
import play.api.libs.json.Json
import support.UnitSpec
import v2.retrieveLateAccountingDateRule.model.response.RetrieveLateAccountingDateRuleResponse

class RetrieveLateAccountingDateResponseSpec extends UnitSpec {

  private val downstreamResponse = Json.parse("""
      |{
      |  "selfEmployments": [
      |    {
      |      "incomeSourceId": "AT0000000000001",
      |      "incomeSourceName": "string",
      |      "cessationDate": "2019-08-24",
      |      "commencementDate": "2019-08-24",
      |      "latency": {},
      |      "accountingPeriodStartDate": "2019-08-24",
      |      "accountingPeriodEndDate": "2019-08-24",
      |      "accountingType": "CASH",
      |      "quarterReporting": {},
      |      "basisPeriodStartDate": "2019-08-24",
      |      "basisPeriodEndDate": "2019-08-24",
      |      "obligations": [],
      |      "lateAccountingDate": {
      |        "eligible": true,
      |        "disapply": true,
      |        "taxYearOfElection": "25-26",
      |        "taxYearElectionExpires": "25-26"
      |      }
      |    }
      |  ]
      | }
      |""".stripMargin)

  private val vendorResponse = Json.parse("""
      |{
      |    "disapply": true,
      |    "eligible": true,
      |    "taxYearOfElection": "2025-26",
      |    "taxYearElectionExpires": "2025-26"
      |}
      |""".stripMargin)

  private val parsedResponse =
    RetrieveLateAccountingDateRuleResponse(disapply = true, eligible = true, Some(TaxYear("2026")), Some(TaxYear("2026")))

  "RetrieveLateAccountingDateRuleResponse" when {
    "There are periods of accounts dates" should {
      "read from json" in {
        downstreamResponse.as[RetrieveLateAccountingDateRuleResponse] shouldBe parsedResponse
      }

      "write to json" in {
        Json.toJson(parsedResponse) shouldBe vendorResponse
      }
    }
  }

}
