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

package v2.createUpdatePeriodsOfAccount

import api.controllers.validators.Validator
import config.MockAppConfig
import support.UnitSpec
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequest
import v2.fixtures.CreateUpdatePeriodsOfAccountFixtures.validFullRequestBodyJson

class CreateUpdatePeriodsOfAccountValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private val validNino: String       = "AA123456A"
  private val validBusinessId: String = "X0IS12345678901"
  private val validTaxYear: String    = "2025-26"

  private val validatorFactory: CreateUpdatePeriodsOfAccountValidatorFactory = new CreateUpdatePeriodsOfAccountValidatorFactory

  "validator()" when {
    "given any tax year" should {
      "return the CreateUpdatePeriodsOfAccountValidator" in {
        MockedAppConfig.accountingTypeMinimumTaxYear.returns(2025).anyNumberOfTimes()
        val result: Validator[CreateUpdatePeriodsOfAccountRequest] = validatorFactory.validator(
          nino = validNino,
          businessId = validBusinessId,
          taxYear = validTaxYear,
          body = validFullRequestBodyJson
        )

        result shouldBe a[CreateUpdatePeriodsOfAccountValidator]
      }
    }
  }

}
