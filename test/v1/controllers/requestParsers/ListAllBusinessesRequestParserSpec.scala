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

package v1.controllers.requestParsers

import api.models.domain.Nino
import api.models.errors.{ErrorWrapper, NinoFormatError}
import support.UnitSpec
import v1.mocks.validators.ListAllBusinessesMockValidator
import v1.models.request.listAllBusinesses.{ListAllBusinessesRawData, ListAllBusinessesRequest}

class ListAllBusinessesRequestParserSpec extends UnitSpec {

  private val nino                   = "AA123456A"
  private val data                   = ListAllBusinessesRawData(nino)
  private val invalidNinoData        = ListAllBusinessesRawData("beans")
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test extends ListAllBusinessesMockValidator {
    lazy val parser = new ListAllBusinessesRequestParser(mockValidator)
  }

  "parse" should {
    "return a ListAllBusinessesRequest" when {
      "the validator returns no errors" in new Test {
        ListAllBusinessesMockValidator.validate(data).returns(Nil)
        parser.parseRequest(data) shouldBe Right(ListAllBusinessesRequest(Nino(nino)))
      }
    }
    "return an error wrapper" when {
      "the validator returns a single error" in new Test {
        ListAllBusinessesMockValidator.validate(invalidNinoData).returns(List(NinoFormatError))
        parser.parseRequest(invalidNinoData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
    }
  }

}
