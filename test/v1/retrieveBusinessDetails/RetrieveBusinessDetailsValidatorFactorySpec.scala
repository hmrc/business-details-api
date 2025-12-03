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

package v1.retrieveBusinessDetails

import api.models.domain.{BusinessId, Nino}
import api.models.errors.*
import support.UnitSpec
import v1.retrieveBusinessDetails.model.request.RetrieveBusinessDetailsRequestData

class RetrieveBusinessDetailsValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "X0IS12345678901"

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)

  val validatorFactory = new RetrieveBusinessDetailsValidatorFactory

  private def validator(nino: String, businessId: String) = validatorFactory.validator(nino, businessId)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result = validator(validNino, validBusinessId).validateAndWrapResult()

        result shouldBe Right(RetrieveBusinessDetailsRequestData(parsedNino, parsedBusinessId))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result = validator("invalid", validBusinessId).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }

      "passed an invalid business id" in {
        val result = validator(validNino, "invalid").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, BusinessIdFormatError)
        )
      }
    }

    "return multiple errors" when {
      "passed multiple invalid fields" in {
        val result = validator("invalid", "invalid").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(BusinessIdFormatError, NinoFormatError))
          )
        )
      }
    }
  }

}
