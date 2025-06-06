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

package v2.retrieveAccountingType

import api.controllers.validators.Validator
import config.MockAppConfig
import support.UnitSpec
import v2.retrieveAccountingType.model.request.RetrieveAccountingTypeRequest

class RetrieveAccountingTypeValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private val validNino       = "AA123456A"
  private val validBusinessId = "X0IS12345678901"
  private val validTaxYear    = "2024-25"

  class Test {

    MockedAppConfig.accountingTypeMinimumTaxYear
      .returns(2025)
      .anyNumberOfTimes()

  }

  private val validatorFactory = new RetrieveAccountingTypeValidatorFactory

  "validator()" when {
    "given any tax year" should {
      "return the Validator for Update Accounting Type" in new Test {
        val result: Validator[RetrieveAccountingTypeRequest] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear)
        result shouldBe a[RetrieveAccountingTypeValidator]
      }
    }
  }

}
