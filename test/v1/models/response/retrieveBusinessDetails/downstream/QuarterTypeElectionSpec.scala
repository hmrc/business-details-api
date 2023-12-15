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

package v1.models.response.retrieveBusinessDetails.downstream

import api.models.domain.TaxYear
import play.api.libs.json.Json
import support.UnitSpec

class QuarterTypeElectionSpec extends UnitSpec {

  private val quarterReportingType = QuarterReportingType.`STANDARD`
  private val taxYearOfElection    = TaxYear.fromMtd("2023-24")

  private val downstreamJson = Json.parse("""
     |{
     | "quarterReportingType": "STANDARD",
     | "taxYearOfElection": "2024"
     |}
     |""".stripMargin)

  private val model = QuarterTypeElection(quarterReportingType, taxYearOfElection)

  private val mtdJson = Json.parse("""
      |{
      | "quarterlyPeriodType": "standard",
      | "taxYearOfChoice": "2023-24"
      |}
      |""".stripMargin)

  "reads" should {
    "read a model from JSON" in {
      downstreamJson.as[QuarterTypeElection] shouldBe model
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(model) shouldBe mtdJson
    }
  }

}
