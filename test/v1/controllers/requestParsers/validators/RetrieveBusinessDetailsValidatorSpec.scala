/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.models.errors.{BusinessIdFormatError, NinoFormatError}
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRawData

class RetrieveBusinessDetailsValidatorSpec extends UnitSpec {

  private val validNino         = "AA123456A"
  private val invalidNino       = "walrus"
  private val validBusinessId   = "X0IS12345678901"
  private val invalidBusinessId = "XWalrusX"

  val validator = new RetrieveBusinessDetailsValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(RetrieveBusinessDetailsRawData(validNino, validBusinessId)) shouldBe empty
      }
    }
    "return an error" when {
      "the provided nino is invalid" in {
        validator.validate(RetrieveBusinessDetailsRawData(invalidNino, validBusinessId)) shouldBe List(NinoFormatError)
      }
      "the provided business id is invalid" in {
        validator.validate(RetrieveBusinessDetailsRawData(validNino, invalidBusinessId)) shouldBe List(BusinessIdFormatError)
      }
    }
    "return multiple errors" when {
      "the provided nino and business id is invalid" in {
        validator.validate(RetrieveBusinessDetailsRawData(invalidNino, invalidBusinessId)) shouldBe List(NinoFormatError, BusinessIdFormatError)
      }
    }
  }

}
