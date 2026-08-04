/*
 * Copyright 2026 HM Revenue & Customs
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

package v2.retrieveLateAccountingDateRule.fixture

import api.models.domain.TaxYear
import play.api.libs.json.{JsValue, Json}
import v2.retrieveLateAccountingDateRule.model.response.{LateAccountingDate, RetrieveLateAccountingDateRuleResponse}

object RetrieveLateAccountingDateFixture {

  val lateAccountingDateModel: LateAccountingDate = LateAccountingDate(
    disapply = true,
    eligible = true,
    taxYearOfElection = Some(TaxYear.fromMtd("2025-26")),
    taxYearElectionExpires = Some(TaxYear.fromMtd("2029-30"))
  )

  val responseModel: RetrieveLateAccountingDateRuleResponse = RetrieveLateAccountingDateRuleResponse(
    lateAccountingDate = Some(lateAccountingDateModel)
  )

  val mtdResponseJson: JsValue = Json.parse(
    """
      |{
      |  "disapply": true,
      |  "eligible": true,
      |  "taxYearOfElection": "2025-26",
      |  "taxYearElectionExpires": "2029-30"
      |}
    """.stripMargin
  )

  val lateAccountingDateDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "eligible": true,
      |  "disapply": true,
      |  "taxYearOfElection": "25-26",
      |  "taxYearElectionExpires": "29-30"
      |}
    """.stripMargin
  )

  val downstreamResponseJson: JsValue = Json.parse(
    s"""
      |{
      |  "selfEmployments": [
      |    {
      |      "incomeSourceId": "XAIS12345678910",
      |      "accountingPeriodStartDate": "2025-04-06",
      |      "accountingPeriodEndDate": "2026-04-05",
      |      "accountingType": "CASH",
      |      "lateAccountingDate": $lateAccountingDateDownstreamJson
      |    }
      |  ]
      |}
    """.stripMargin
  )

}
