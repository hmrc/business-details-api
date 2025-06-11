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

package v2.retrievePeriodsOfAccount

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import support.UnitSpec
import v2.retrievePeriodsOfAccount.model.request.RetrievePeriodsOfAccountRequest

class RetrievePeriodsOfAccountValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino        = "AA123456A"
  private val validBusinessId  = "X0IS12345678901"
  private val validTaxYear     = "2025-26"
  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, businessId: String, taxYear: String) =
    new RetrievePeriodsOfAccountValidator(nino, businessId, taxYear)

  "validator()" should {
    "passed a valid request" when {
      "return the parsed domain object" in {
        val result = validator(validNino, validBusinessId, validTaxYear).validateAndWrapResult()

        result shouldBe Right(RetrievePeriodsOfAccountRequest(parsedNino, parsedBusinessId, parsedTaxYear))
      }
    }
  }

  "return a single error" when {
    "passed an invalid nino" in {
      val result = validator("invalid", validBusinessId, validTaxYear).validateAndWrapResult()
      result shouldBe Left(
        ErrorWrapper(correlationId, NinoFormatError)
      )
    }

    "passed an invalid business id" in {
      val result = validator(validNino, "invalid", validTaxYear).validateAndWrapResult()
      result shouldBe Left(
        ErrorWrapper(correlationId, BusinessIdFormatError)
      )
    }

    "passed an invalid tax year" in {
      val result = validator(validNino, validBusinessId, "invalid").validateAndWrapResult()
      result shouldBe Left(
        ErrorWrapper(correlationId, TaxYearFormatError)
      )
    }

    "passed an unsupported tax year" in {
      val result = validator(validNino, validBusinessId, "2024-25").validateAndWrapResult()
      result shouldBe Left(
        ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
      )
    }

    "passed an invalid range tax year" in {
      val result = validator(validNino, validBusinessId, "2024-26").validateAndWrapResult()
      result shouldBe Left(
        ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
      )
    }
  }

  "return multiple errors" when {
    "passed multiple invalid fields" in {
      val result = validator("invalid", "invalid", "invalid").validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(
          correlationId,
          BadRequestError,
          Some(List(BusinessIdFormatError, NinoFormatError, TaxYearFormatError))
        )
      )
    }
  }

}
