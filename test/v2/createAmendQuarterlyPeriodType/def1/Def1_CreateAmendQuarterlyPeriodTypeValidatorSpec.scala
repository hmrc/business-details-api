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

package v2.createAmendQuarterlyPeriodType.def1

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.utils.JsonErrorValidators
import play.api.libs.json._
import support.UnitSpec
import v2.createAmendQuarterlyPeriodType.def1.model.request._

class Def1_CreateAmendQuarterlyPeriodTypeValidatorSpec extends UnitSpec with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "X0IS12345678901"
  private val validTaxYear    = "2023-24"

  private val validBody = Json.parse("""
                                       |{
                                       | "quarterlyPeriodType": "standard"
                                       |}
                                       |""".stripMargin)

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private val parsedBody = Def1_CreateAmendQuarterlyPeriodTypeRequestBody(QuarterlyPeriodType.`standard`)

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue) =
    new Def1_CreateAmendQuarterlyPeriodTypeValidator(nino, businessId, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result = validator(validNino, validBusinessId, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAmendQuarterlyPeriodTypeRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result = validator("invalid", validBusinessId, validTaxYear, validBody).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }

      "passed an invalid business id" in {
        val result = validator(validNino, "invalid", validTaxYear, validBody).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, BusinessIdFormatError)
        )
      }

      "passed an invalid tax year" in {
        val result = validator(validNino, validBusinessId, "invalid", validBody).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }

      "passed an invalid request body" in {
        val invalidBody = JsObject.empty
        val result      = validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }

      "passed a request body with an invalidly formatted quarterlyPeriodType" in {
        val invalidBody = validBody.update("quarterlyPeriodType", JsBoolean(true))
        val result      = validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/quarterlyPeriodType"))
        )
      }

      "passed a request body with a non-enum value for quarterlyPeriodType" in {
        val invalidBody = validBody.update("quarterlyPeriodType", JsString("Standard"))
        val result      = validator(validNino, validBusinessId, validTaxYear, invalidBody).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/quarterlyPeriodType"))
        )
      }
    }

    "return multiple errors" when {
      "passed multiple invalid fields" in {
        val result = validator("invalid", "invalid", "invalid", JsObject.empty).validateAndWrapResult()

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
