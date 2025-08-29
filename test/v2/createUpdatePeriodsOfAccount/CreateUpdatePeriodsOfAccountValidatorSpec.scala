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

package v2.createUpdatePeriodsOfAccount

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.utils.JsonErrorValidators
import config.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v2.createUpdatePeriodsOfAccount.request.{CreateUpdatePeriodsOfAccountRequest, CreateUpdatePeriodsOfAccountRequestBody}
import v2.fixtures.CreateUpdatePeriodsOfAccountFixtures._

class CreateUpdatePeriodsOfAccountValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private implicit val correlationId: String = "1234"

  private val validNino: String       = "AA123456A"
  private val validBusinessId: String = "X0IS12345678901"
  private val validTaxYear: String    = "2025-26"

  private val parsedNino: Nino             = Nino(validNino)
  private val parsedBusinessId: BusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear: TaxYear       = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, businessId: String, taxYear: String, body: JsValue): CreateUpdatePeriodsOfAccountValidator =
    new CreateUpdatePeriodsOfAccountValidator(nino, businessId, taxYear, body)

  private trait Test {
    MockedAppConfig.accountingTypeMinimumTaxYear.returns(2025).anyNumberOfTimes()
  }

  "running a validation" should {
    "return no errors" when {
      "a full valid request is provided with periodsOfAccount set to true and periodsOfAccountDates" when {
        "all start dates are within the supplied tax year" in new Test {
          val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
            validator(validNino, validBusinessId, validTaxYear, validFullRequestBodyJson).validateAndWrapResult()

          result shouldBe Right(CreateUpdatePeriodsOfAccountRequest(parsedNino, parsedBusinessId, parsedTaxYear, fullRequestBodyModel))
        }

        "a start date is before the start of the supplied tax year" in new Test {
          val validJson: JsValue = validFullRequestBodyJson.update(
            "/periodsOfAccountDates",
            Json.arr(
              Json.obj("startDate" -> "2025-03-29", "endDate" -> "2025-07-05"),
              Json.obj("startDate" -> "2025-07-06", "endDate" -> "2025-10-05")
            )
          )

          val validModel: CreateUpdatePeriodsOfAccountRequestBody = fullRequestBodyModel.copy(
            periodsOfAccountDates = fullRequestBodyModel.periodsOfAccountDates.map { dates =>
              dates.updated(0, dates.head.copy(startDate = "2025-03-29"))
            }
          )

          val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
            validator(validNino, validBusinessId, validTaxYear, validJson).validateAndWrapResult()

          result shouldBe Right(CreateUpdatePeriodsOfAccountRequest(parsedNino, parsedBusinessId, parsedTaxYear, validModel))
        }
      }

      "a minimum valid request is provided with only periodsOfAccount set to false" in new Test {
        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, validTaxYear, validMinimumRequestBodyJson).validateAndWrapResult()

        result shouldBe Right(CreateUpdatePeriodsOfAccountRequest(parsedNino, parsedBusinessId, parsedTaxYear, minimumRequestBodyModel))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator("A12344A", validBusinessId, validTaxYear, validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return BusinessIdFormatError error" when {
      "an invalid business ID is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, "X0IS", validTaxYear, validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, "20256", validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, "2025-27", validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an unsupported tax year is supplied" in new Test {
        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, "2023-24", validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "passed an empty body" in new Test {
        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, validTaxYear, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body with an empty periodsOfAccountDates array" in new Test {
        val invalidJson: JsValue = validFullRequestBodyJson.update("/periodsOfAccountDates", JsArray.empty)

        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/periodsOfAccountDates")))
      }

      "passed a body with a missing mandatory periodsOfAccount field" in new Test {
        val invalidJson: JsValue = validFullRequestBodyJson.removeProperty("/periodsOfAccount")

        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/periodsOfAccount")))
      }

      validFullRequestBodyJson.as[JsObject].fields.foreach { case (field, _) =>
        s"passed a body with an incorrect type for field $field" in new Test {
          val invalidJson: JsValue = validFullRequestBodyJson.update(s"/$field", JsObject.empty)

          val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
            validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath(s"/$field")))
        }
      }
    }

    "return RulePeriodsOfAccountError error" when {
      "passed a body with only periodsOfAccount set to true" in new Test {
        val invalidJson: JsValue = validFullRequestBodyJson.removeProperty("/periodsOfAccountDates")

        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RulePeriodsOfAccountError))
      }

      "passed a body with periodsOfAccount set to false and periodsOfAccountDates" in new Test {
        val invalidJson: JsValue = validFullRequestBodyJson.update("/periodsOfAccount", JsBoolean(false))

        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RulePeriodsOfAccountError))
      }
    }

    "return StartDateFormatError error" when {
      "passed a body with an incorrectly formatted start date" in new Test {
        val invalidJson: JsValue = validFullRequestBodyJson.update(
          "/periodsOfAccountDates",
          Json.arr(Json.obj("startDate" -> "2025", "endDate" -> "2025-07-05"))
        )

        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, StartDateFormatError.withPath("/periodsOfAccountDates/0/startDate")))
      }
    }

    "return EndDateFormatError error" when {
      "passed a body with an incorrectly formatted end date" in new Test {
        val invalidJson: JsValue = validFullRequestBodyJson.update(
          "/periodsOfAccountDates",
          Json.arr(Json.obj("startDate" -> "2025-04-06", "endDate" -> "2025"))
        )

        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, EndDateFormatError.withPath("/periodsOfAccountDates/0/endDate")))
      }
    }

    "return RuleEndBeforeStartDateError error" when {
      "passed a body with an endDate that is before startDate" in new Test {
        val invalidJson: JsValue = validFullRequestBodyJson.update(
          "/periodsOfAccountDates",
          Json.arr(Json.obj("startDate" -> "2025-07-06", "endDate" -> "2025-07-05"))
        )

        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleEndBeforeStartDateError.withPath("/periodsOfAccountDates/0")))
      }
    }

    "return RulePeriodsOverlapError error" when {
      val overlappingPeriodCases: Seq[(String, JsArray)] = Seq(
        "overlapping periods (forward order)" -> Json.arr(
          Json.obj("startDate" -> "2025-04-06", "endDate" -> "2025-07-05"),
          Json.obj("startDate" -> "2025-06-06", "endDate" -> "2025-10-05")
        ),
        "overlapping periods (reverse order)" -> Json.arr(
          Json.obj("startDate" -> "2025-06-06", "endDate" -> "2025-10-05"),
          Json.obj("startDate" -> "2025-04-06", "endDate" -> "2025-07-05")
        ),
        "overlapping periods (same period)" -> Json.arr(
          Json.obj("startDate" -> "2025-04-06", "endDate" -> "2025-07-05"),
          Json.obj("startDate" -> "2025-04-06", "endDate" -> "2025-07-05")
        )
      )

      def invalidJson(overlappingPeriods: JsArray): JsValue = validFullRequestBodyJson.update(
        "/periodsOfAccountDates",
        overlappingPeriods
      )

      overlappingPeriodCases.foreach { case (scenario, periods) =>
        s"passed a body with $scenario" in new Test {
          val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
            validator(validNino, validBusinessId, validTaxYear, invalidJson(periods)).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, RulePeriodsOverlapError.withPath("/periodsOfAccountDates")))
        }
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator("A12344A", "X0IS", "20256", validFullRequestBodyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(BusinessIdFormatError, NinoFormatError, TaxYearFormatError))))
      }

      "passed a body with start and end dates outside the valid tax year bounds" in new Test {
        val invalidJson: JsValue = validFullRequestBodyJson.update(
          "/periodsOfAccountDates",
          Json.arr(Json.obj("startDate" -> "2026-04-06", "endDate" -> "2026-04-06"))
        )

        val result: Either[ErrorWrapper, CreateUpdatePeriodsOfAccountRequest] =
          validator(validNino, validBusinessId, validTaxYear, invalidJson).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(
              List(
                RuleEndDateError.withPath("/periodsOfAccountDates/0/endDate"),
                RuleStartDateError.withPath("/periodsOfAccountDates/0/startDate")
              )
            )
          )
        )
      }
    }
  }

}
