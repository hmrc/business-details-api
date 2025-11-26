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

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.*
import api.utils.JsonErrorValidators
import config.MockAppConfig
import play.api.libs.json.*
import support.UnitSpec
import v2.common.models.AccountingType
import v2.updateAccountingType.model.request.{UpdateAccountingTypeRequestBody, UpdateAccountingTypeRequestData}

class UpdateAccountingTypeValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private implicit val correlationId: String = "1234"

  private val validNino: String       = "AA123456A"
  private val validBusinessId: String = "X0IS12345678901"
  private val validTaxYear: String    = "2024-25"

  private val parsedNino: Nino             = Nino(validNino)
  private val parsedBusinessId: BusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear: TaxYear       = TaxYear.fromMtd(validTaxYear)

  private val validBody: JsValue = Json.parse(
    """
      |{
      |  "accountingType": "CASH"
      |}
    """.stripMargin
  )

  private val parsedBody: UpdateAccountingTypeRequestBody = UpdateAccountingTypeRequestBody(AccountingType.CASH)

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue, temporalValidationEnabled: Boolean = true) =
    new UpdateAccountingTypeValidator(nino, businessId, taxYear, body, temporalValidationEnabled)(mockAppConfig)

  private trait Test {
    MockedAppConfig.accountingTypeMinimumTaxYear.returns(2025).anyNumberOfTimes()
  }

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request with a tax year that has ended and temporal validation is enabled" in new Test {
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(UpdateAccountingTypeRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }

      "passed a valid request with a tax year that has not ended and temporal validation is disabled" in new Test {
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] = validator(
          validNino,
          validBusinessId,
          TaxYear.currentTaxYear.asMtd,
          validBody,
          temporalValidationEnabled = false
        ).validateAndWrapResult()

        result shouldBe Right(UpdateAccountingTypeRequestData(parsedNino, parsedBusinessId, TaxYear.currentTaxYear, parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in new Test {
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator("invalid", validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }

      "passed an invalid business id" in new Test {
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator(validNino, "invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, BusinessIdFormatError)
        )
      }

      "passed an invalid tax year" in new Test {
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator(validNino, validBusinessId, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }

      "passed an unsupported tax year" in new Test {
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator(validNino, validBusinessId, "2023-24", validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }

      "passed an tax year that has not ended and temporal validation is enabled" in new Test {
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator(validNino, validBusinessId, TaxYear.currentTaxYear.asMtd, validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotEndedError)
        )
      }

      "passed an invalid range tax year" in new Test {
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator(validNino, validBusinessId, "2024-26", validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }

      "passed an invalid request body" in new Test {
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator(validNino, validBusinessId, validTaxYear, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }

      "passed a request body with an invalidly formatted accountingType" in new Test {
        val invalidBody: JsValue = validBody.update("accountingType", JsBoolean(true))
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/accountingType"))
        )
      }

      "passed a request body with a non-enum value for accountingType" in new Test {
        val invalidBody: JsValue = validBody.update("accountingType", JsString("cash"))

        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/accountingType"))
        )
      }
    }

    "return multiple errors" when {
      "passed multiple invalid fields" in new Test {
        val result: Either[ErrorWrapper, UpdateAccountingTypeRequestData] =
          validator("invalid", "invalid", "invalid", JsObject.empty).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(BusinessIdFormatError, NinoFormatError, TaxYearFormatError, RuleIncorrectOrEmptyBodyError))
          )
        )
      }
    }
  }

}
