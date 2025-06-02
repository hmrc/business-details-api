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

package v2.updateAccountingType.model.request

import play.api.libs.json.Json
import support.UnitSpec
import v2.common.models.AccountingType

class UpdateAccountingTypeRequestBodySpec extends UnitSpec {

  private val validRequestBodyCASH = Json.parse("""
      |{
      | "accountingType": "CASH"
      |}
      |""".stripMargin)

  private val downstreamRequestBodyCASH = Json.parse("""
      |{
      | "accountingType": "CASH"
      |}
      |""".stripMargin)

  private val validRequestBodyACCRUAL = Json.parse("""
      |{
      | "accountingType": "ACCRUAL"
      |}
      |""".stripMargin)

  private val downstreamRequestBodyACCRUAL = Json.parse("""
      |{
      | "accountingType": "ACCRUAL"
      |}
      |""".stripMargin)

  private val parsedRequestBodyCASH = UpdateAccountingTypeRequestBody(AccountingType.CASH)

  private val parsedRequestBodyACCRUAL = UpdateAccountingTypeRequestBody(AccountingType.ACCRUAL)

  "UpdateAccountingType" should {
    "read from vendor JSON with CASH enum" in {
      validRequestBodyCASH.as[UpdateAccountingTypeRequestBody] shouldBe parsedRequestBodyCASH
    }

    "write to downstream Json with CASH enum" in {
      Json.toJson(parsedRequestBodyCASH) shouldBe downstreamRequestBodyCASH
    }
  }

  "UpdateAccountingType with ACCRUAL enum" should {
    "read from vendor JSON" in {
      validRequestBodyACCRUAL.as[UpdateAccountingTypeRequestBody] shouldBe parsedRequestBodyACCRUAL
    }

    "write to downstream Json with ACCRUAL enum" in {
      Json.toJson(parsedRequestBodyACCRUAL) shouldBe downstreamRequestBodyACCRUAL
    }
  }

}
