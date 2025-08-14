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

package v2.disapplyLateAccountingDateRule

import api.controllers.validators.Validator
import config.MockAppConfig
import support.UnitSpec
import v2.disapplyLateAccountingDateRule.model.request.DisapplyLateAccountingDateRuleRequest

class DisapplyLateAccountingDateRuleValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private val validNino       = "AA123456A"
  private val validBusinessId = "X0IS12345678901"
  private val validTaxYear    = "2024-25"
  private val returnedTaxYear = 2025

  private val validatorFactory = new DisapplyLateAccountingDateRuleValidatorFactory

  "validator()" when {
    "given any tax year" should {

      "return the Validator for Disapply Late Accounting Date Rule" in {
        MockedAppConfig.accountingTypeMinimumTaxYear.returns(returnedTaxYear).anyNumberOfTimes()

        val result: Validator[DisapplyLateAccountingDateRuleRequest] =
          validatorFactory.validator(nino = validNino, businessId = validBusinessId, taxYear = validTaxYear, temporalValidationEnabled = true)

        result shouldBe a[DisapplyLateAccountingDateRuleValidator]
      }
    }
  }

}
