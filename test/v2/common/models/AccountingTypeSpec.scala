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

import play.api.libs.json.*
import support.UnitSpec
import v2.common.models.AccountingType.*

class AccountingTypeSpec extends UnitSpec {

  private val allQuarterlyPeriodTypes: List[AccountingType] = List(CASH, ACCRUAL)

  "QuarterlyPeriodType" should {
    allQuarterlyPeriodTypes.foreach { accountingType =>
      s"read $accountingType from JSON correctly" in {
        val json: JsValue                    = JsString(accountingType.toString)
        val result: JsResult[AccountingType] = Json.fromJson[AccountingType](json)

        result shouldBe JsSuccess(accountingType)
      }

      s"write $accountingType to downstream format correctly" in {
        val downstreamJson: JsValue = JsString(accountingType.toString)
        Json.toJson(accountingType) shouldBe downstreamJson
      }
    }

    "return a JsError" when {
      "reading an invalid QuarterlyPeriodType" in {
        val json: JsValue                    = JsString("Standard")
        val result: JsResult[AccountingType] = Json.fromJson[AccountingType](json)

        result shouldBe JsError(JsPath, JsonValidationError("error.expected.AccountingType"))
      }
    }
  }

}
