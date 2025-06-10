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

package api.controllers.validators.resolvers

import api.controllers.validators.resolvers
import api.models.domain.DateRange
import api.models.errors.{EndDateFormatError, MtdError, RuleEndBeforeStartDateError, StartDateFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

import java.time.LocalDate

class ResolveDateRangeSpec extends UnitSpec {

  private val validStart: String = "2023-06-21"
  private val validEnd: String   = "2024-06-21"

  private val sameValidStart: String = "2023-06-21"
  private val sameValidEnd: String   = "2023-06-21"

  private val startDateFormatError: MtdError    = StartDateFormatError.withPath("pathToStartDate")
  private val endDateFormatError: MtdError      = EndDateFormatError.withPath("pathToEndDate")
  private val endBeforeStartDateError: MtdError = RuleEndBeforeStartDateError.withPath("somePath")

  private val resolveDateRange: ResolveDateRange = ResolveDateRange(
    startDateFormatError = startDateFormatError,
    endDateFormatError = endDateFormatError,
    endBeforeStartDateError = endBeforeStartDateError
  )

  "ResolveDateRange" should {
    "return no errors" when {
      "passed a valid start and end date" in {
        val result: Validated[Seq[MtdError], DateRange] = resolveDateRange(validStart -> validEnd)
        result shouldBe Valid(DateRange(LocalDate.parse(validStart), LocalDate.parse(validEnd)))
      }

      "passed an end date equal to start date" in {
        val result: Validated[Seq[MtdError], DateRange] = resolveDateRange(sameValidStart -> sameValidEnd)
        result shouldBe Valid(DateRange(LocalDate.parse(sameValidStart), LocalDate.parse(sameValidEnd)))
      }
    }

    "return an error" when {
      "passed an invalid start date" in {
        val result: Validated[Seq[MtdError], DateRange] = resolveDateRange("not-a-date" -> validEnd)
        result shouldBe Invalid(List(startDateFormatError))
      }

      "passed an invalid end date" in {
        val result: Validated[Seq[MtdError], DateRange] = resolveDateRange(validStart -> "not-a-date")
        result shouldBe Invalid(List(endDateFormatError))
      }

      "passed an end date before start date" in {
        val result: Validated[Seq[MtdError], DateRange] = resolveDateRange(validEnd -> validStart)
        result shouldBe Invalid(List(endBeforeStartDateError))
      }
    }
  }

  "ResolveDateRange withDatesLimitedTo" should {
    val minDate: LocalDate = LocalDate.parse("2000-02-01")
    val maxDate: LocalDate = LocalDate.parse("2000-02-10")
    val resolverWithLimits: resolveDateRange.Resolver[(String, String), DateRange] =
      resolveDateRange.withDatesLimitedTo(minDate = minDate, maxDate = maxDate)

    "return no errors for dates within limits" in {
      val result: Validated[Seq[MtdError], DateRange] = resolverWithLimits("2000-02-01" -> "2000-02-10")
      result shouldBe Valid(DateRange(minDate, maxDate))
    }

    "return an error for dates outside the limits" when {
      "the start date is too early" in {
        val result: Validated[Seq[MtdError], DateRange] = resolverWithLimits("1999-12-31" -> "2000-02-10")
        result shouldBe Invalid(List(startDateFormatError))
      }

      "the end date is too late" in {
        val result: Validated[Seq[MtdError], DateRange] = resolverWithLimits("2000-02-01" -> "2000-02-11")
        result shouldBe Invalid(List(endDateFormatError))
      }

      "both start and end dates are outside the limits" in {
        val result: Validated[Seq[MtdError], DateRange] = resolverWithLimits("1999-12-31" -> "2000-02-11")
        result shouldBe Invalid(List(startDateFormatError, endDateFormatError))
      }
    }
  }

  "ResolveDateRange datesLimitedTo" should {
    val minDate: LocalDate = LocalDate.parse("2000-02-01")
    val maxDate: LocalDate = LocalDate.parse("2000-02-10")
    val validator: resolvers.ResolveDateRange.Validator[DateRange] = ResolveDateRange.datesLimitedTo(
      minDate = minDate,
      minError = startDateFormatError,
      maxDate = maxDate,
      maxError = endDateFormatError
    )

    val tooEarly: LocalDate = minDate.minusDays(1)
    val tooLate: LocalDate  = maxDate.plusDays(1)

    "allow min and max dates" in {
      val result: Option[Seq[MtdError]] = validator(DateRange(minDate, maxDate))
      result shouldBe None
    }

    "disallow dates earlier than min or later than max" in {
      val result: Option[Seq[MtdError]] = validator(DateRange(tooEarly, tooLate))
      result shouldBe Some(List(startDateFormatError, endDateFormatError))
    }

    "disallow dates later than max" in {
      val result: Option[Seq[MtdError]] = validator(DateRange(tooLate, tooLate))
      result shouldBe Some(List(startDateFormatError, endDateFormatError))
    }

    "disallow dates earlier than min" in {
      val result: Option[Seq[MtdError]] = validator(DateRange(tooEarly, tooEarly))
      result shouldBe Some(List(startDateFormatError, endDateFormatError))
    }
  }

}
