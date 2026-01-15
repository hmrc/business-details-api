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

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import support.UnitSpec
import v2.retrieveAccountingType.model.request.RetrieveAccountingTypeRequest

class RetrieveAccountingTypeValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val nino       = "AA123456A"
  private val businessId = "X0IS12345678901"
  private val taxYear    = "2023-24"

  private def validator(nino: String, businessId: String, taxYear: String) =
    new RetrieveAccountingTypeValidator(nino, businessId, taxYear)

  "validator" should {
    List("2022-23", "2023-24", "2024-25").foreach { taxYear =>
      "return the parsed domain object" when {
        s"given a valid request for taxYear $taxYear" in {
          val result: Either[ErrorWrapper, RetrieveAccountingTypeRequest] = validator(nino, businessId, taxYear).validateAndWrapResult()

          result shouldBe Right(RetrieveAccountingTypeRequest(Nino(nino), BusinessId(businessId), TaxYear.fromMtd(taxYear)))
        }
      }
    }
  }

  "return a single error" when {
    "passed an invalid nino" in {
      val result: Either[ErrorWrapper, RetrieveAccountingTypeRequest] = validator("invalid", businessId, taxYear).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
    }

    "passed an invalid business id" in {
      val result: Either[ErrorWrapper, RetrieveAccountingTypeRequest] = validator(nino, "invalid", taxYear).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
    }

    "passed an invalid tax year" in {
      val result: Either[ErrorWrapper, RetrieveAccountingTypeRequest] = validator(nino, businessId, "invalid").validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
    }

    "passed an invalid range tax year" in {
      val result: Either[ErrorWrapper, RetrieveAccountingTypeRequest] = validator(nino, businessId, "2024-26").validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
    }
  }

  "return multiple errors" when {
    "passed multiple invalid fields" in {
      val result: Either[ErrorWrapper, RetrieveAccountingTypeRequest] = validator("invalid", "invalid", "invalid").validateAndWrapResult()

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
