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

package v2.updateAccountingType

import api.controllers.validators.Validator
import config.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v2.updateAccountingType.model.request.UpdateAccountingTypeRequestData

class UpdateAccountingTypeValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private val validNino: String = "AA123456A"
  private val validBusinessId: String = "X0IS12345678901"
  private val validTaxYear: String = "2024-25"

  private val validBody: JsValue = Json.parse(
    """
      |{
      |  "accountingType": "CASH"
      |}
    """.stripMargin
  )

  private val validatorFactory: UpdateAccountingTypeValidatorFactory = new UpdateAccountingTypeValidatorFactory

  "validator()" when {
    "given any tax year" should {
      "return the Validator for Update Accounting Type" in {
        MockedAppConfig.accountingTypeMinimumTaxYear.returns(2025).anyNumberOfTimes()

        val result: Validator[UpdateAccountingTypeRequestData] = validatorFactory.validator(
          nino = validNino,
          businessId = validBusinessId,
          taxYear = validTaxYear,
          body = validBody,
          temporalValidationEnabled = true
        )

        result shouldBe a[UpdateAccountingTypeValidator]
      }
    }
  }

}
