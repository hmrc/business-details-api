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

package v2.createAmendQuarterlyPeriodType

import api.controllers.validators.Validator
import api.utils.JsonErrorValidators
import play.api.libs.json.*
import support.UnitSpec
import v2.createAmendQuarterlyPeriodType.def1.Def1_CreateAmendQuarterlyPeriodTypeValidator
import v2.createAmendQuarterlyPeriodType.model.request.CreateAmendQuarterlyPeriodTypeRequestData

class CreateAmendQuarterlyPeriodTypeValidatorFactorySpec extends UnitSpec with JsonErrorValidators {

  private val validNino       = "AA123456A"
  private val validBusinessId = "X0IS12345678901"
  private val validTaxYear    = "2023-24"

  private val validBody = Json.parse("""
      |{
      | "quarterlyPeriodType": "standard"
      |}
      |""".stripMargin)

  private val validatorFactory = new CreateAmendQuarterlyPeriodTypeValidatorFactory

  "validator()" when {
    "given any tax year" should {
      "return the Validator for schema definition 1" in {
        val requestBody = validBody
        val result: Validator[CreateAmendQuarterlyPeriodTypeRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear, requestBody)
        result shouldBe a[Def1_CreateAmendQuarterlyPeriodTypeValidator]
      }
    }
  }

}
