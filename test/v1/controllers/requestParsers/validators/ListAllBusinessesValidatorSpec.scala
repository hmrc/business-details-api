/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.models.errors.NinoFormatError
import v1.models.request.listAllBusinesses.ListAllBusinessesRawData

class ListAllBusinessesValidatorSpec extends UnitSpec{

  private val validNino     = "AA123456A"
  private val invalidNino   = "beans"

  private val listAllBusinessesRawData: (String) => ListAllBusinessesRawData = (nino) => ListAllBusinessesRawData(nino)

  val validator = new ListAllBusinessesValidator

  "list all businesses validation" should {
    "return no errors" when {
      "supplied with a valid nino" in {
        validator.validate(listAllBusinessesRawData(validNino)) shouldBe empty
      }
    }
    "return a FORMAT_NINO error" when {
      "the provided nino is invalid" in {
        validator.validate(listAllBusinessesRawData(invalidNino)) shouldBe List(NinoFormatError)
      }
    }
  }

}
