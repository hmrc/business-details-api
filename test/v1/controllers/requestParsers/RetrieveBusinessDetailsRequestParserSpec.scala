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
import api.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import support.UnitSpec
import v1.controllers.requestParsers.validators.RetrieveBusinessDetailsMockValidator
import v1.models.request.retrieveBusinessDetails.{RetrieveBusinessDetailsRawData, RetrieveBusinessDetailsRequest}

class RetrieveBusinessDetailsRequestParserSpec extends UnitSpec {

  private val nino                   = "AA123456A"
  private val businessId             = "X0IS123456789012"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val data                   = RetrieveBusinessDetailsRawData(nino, businessId)
  private val invalidSingleData      = RetrieveBusinessDetailsRawData(nino, "Walrus")
  private val invalidMultipleData    = RetrieveBusinessDetailsRawData("Beans", "Walrus")

  trait Test extends RetrieveBusinessDetailsMockValidator {
    lazy val parser = new RetrieveBusinessDetailsRequestParser(mockValidator)
  }

  "parse" should {
    "return a RetrieveBusinessDetailsRequest" when {
      "the validator returns no errors" in new Test {
        RetrieveBusinessDetailsMockValidator.validate(data).returns(Nil)
        parser.parseRequest(data) shouldBe Right(RetrieveBusinessDetailsRequest(Nino(nino), businessId))
      }
    }
    "return an error wrapper" when {
      "the validator returns a single error" in new Test {
        RetrieveBusinessDetailsMockValidator.validate(invalidSingleData).returns(List(NinoFormatError))
        parser.parseRequest(invalidSingleData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
      "the validator returns multiple errors" in new Test {
        RetrieveBusinessDetailsMockValidator.validate(invalidMultipleData).returns(List(NinoFormatError, BusinessIdFormatError))
        parser.parseRequest(invalidMultipleData) shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }

}
