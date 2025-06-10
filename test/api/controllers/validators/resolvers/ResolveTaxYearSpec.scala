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

import api.models.domain.TaxYear
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

import java.time.{Clock, Instant, ZoneOffset}

class ResolveTaxYearSpec extends UnitSpec {

  "ResolveTaxYear" should {
    "return no errors" when {
      val validTaxYear: String = "2018-19"

      "given a valid tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = ResolveTaxYear(validTaxYear)
        result shouldBe Valid(TaxYear.fromMtd(validTaxYear))
      }
    }

    "return an error" when {
      "given an invalid tax year format" in {
        ResolveTaxYear("2019") shouldBe Invalid(List(TaxYearFormatError))
      }

      "given a tax year string in which the range is greater than 1 year" in {
        ResolveTaxYear("2017-19") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the end year is before the start year" in {
        ResolveTaxYear("2018-17") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the start and end years are the same" in {
        ResolveTaxYear("2017-17") shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }

      "the tax year is bad" in {
        ResolveTaxYear("20177-17") shouldBe Invalid(List(TaxYearFormatError))
      }
    }
  }

  "ResolveDetailedTaxYear" should {
    implicit val fixedClock: Clock = Clock.fixed(Instant.parse("2026-08-01T00:00:00Z"), ZoneOffset.UTC)
    val minimumTaxYear: TaxYear    = TaxYear.fromMtd("2025-26")

    def resolver(allowIncompleteTaxYear: Boolean = true): ResolveDetailedTaxYear = ResolveDetailedTaxYear(
      minimumTaxYear = minimumTaxYear,
      allowIncompleteTaxYear = allowIncompleteTaxYear
    )

    "return no errors" when {
      "given the minimum allowed tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("2025-26")
        result shouldBe Valid(minimumTaxYear)
      }

      "given an incomplete tax year but incomplete years are allowed" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("2026-27")
        result shouldBe Valid(TaxYear.fromMtd("2026-27"))
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "given a tax year before the minimum tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("2024-25")
        result shouldBe Invalid(List(RuleTaxYearNotSupportedError))
      }
    }

    "return RuleTaxYearNotEndedError" when {
      "given an incomplete tax year and incomplete years are not allowed" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver(false)("2026-27")
        result shouldBe Invalid(List(RuleTaxYearNotEndedError))
      }
    }

    "return TaxYearFormatError" when {
      "given a badly formatted tax year" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("not-a-tax-year")
        result shouldBe Invalid(List(TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "given a tax year with an invalid range" in {
        val result: Validated[Seq[MtdError], TaxYear] = resolver()("2025-27")
        result shouldBe Invalid(List(RuleTaxYearRangeInvalidError))
      }
    }
  }

}
