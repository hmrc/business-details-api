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

package v2.listAllBusinesses

import api.models.domain.Nino
import api.models.errors._
import support.UnitSpec
import v2.listAllBusinesses.model.request.ListAllBusinessesRequestData

class ListAllBusinessDetailsValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino  = "AA123456A"
  private val parsedNino = Nino(validNino)

  val validatorFactory = new ListAllBusinessDetailsValidatorFactory

  private def validator(nino: String) = validatorFactory.validator(nino)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result = validator(validNino).validateAndWrapResult()

        result shouldBe Right(ListAllBusinessesRequestData(parsedNino))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result = validator("A12344A").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
    }

  }

}
