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

package v1.retrieveBusinessDetails.model.response

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class AccountingPeriodSpec extends UnitSpec {

  val responseBody: AccountingPeriod = AccountingPeriod("2018-04-06", "2019-04-05")

  "reads" should {
    "read from json" when {
      val desJson: JsValue = Json.parse(
        """
          |{
          |  "accountingPeriodStartDate": "2018-04-06",
          |  "accountingPeriodEndDate": "2019-04-05"
          |}
          |""".stripMargin
      )
      "return a valid model" in {
        responseBody shouldBe desJson.as[AccountingPeriod]
      }
    }
  }

}
