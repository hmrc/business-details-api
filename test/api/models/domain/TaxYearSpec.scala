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

package api.models.domain

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

import java.time.{LocalDate, ZoneId}

class TaxYearSpec extends UnitSpec {

  "TaxYear" when {
    val taxYear: TaxYear = TaxYear.fromMtd("2023-24")

    "constructed from an MTD tax year" should {
      "return the year" in {
        taxYear.year shouldBe 2024
      }

      "return the start date" in {
        taxYear.startDate shouldBe LocalDate.parse("2023-04-06")
      }

      "return the end date" in {
        taxYear.endDate shouldBe LocalDate.parse("2024-04-05")
      }

      "return the downstream tax year" in {
        taxYear.asDownstream shouldBe "2024"
      }

      "return the MTD tax year" in {
        taxYear.asMtd shouldBe "2023-24"
      }

      "return the tax year in the 'Tax Year Specific API' format" in {
        taxYear.asTysDownstream shouldBe "23-24"
      }
    }

    "constructed from a starting year" should {
      "return the tax year that begins in that year and ends the following year" in {
        val year: Int        = 2023
        val taxYear: TaxYear = TaxYear.starting(year)
        taxYear.year shouldBe 2024
        taxYear.startYear shouldBe 2023
        taxYear.asMtd shouldBe "2023-24"
      }
    }

    "constructed from an ending year" should {
      "return the tax year that ends in that year and started the previous year" in {
        val year: Int        = 2024
        val taxYear: TaxYear = TaxYear.ending(year)
        taxYear.year shouldBe 2024
        taxYear.startYear shouldBe 2023
        taxYear.asMtd shouldBe "2023-24"
      }
    }

    "constructed from a date" should {
      "be the expected year, taking into account the UK tax year start date" in {
        val testCases: List[(LocalDate, Int)] = List(
          LocalDate.parse("2025-04-05") -> 2025,
          LocalDate.parse("2025-04-06") -> 2026,
          LocalDate.parse("2024-12-01") -> 2025,
          LocalDate.parse("2023-01-01") -> 2023,
          LocalDate.parse("2026-07-10") -> 2027
        )

        testCases.foreach { case (date, expectedYear) =>
          withClue(s"Given $date:") {
            val result: TaxYear = TaxYear.containing(date)
            result.year shouldBe expectedYear
          }
        }
      }
    }

    "constructed from an ISO date" should {
      "be the expected year, taking into account the UK tax year start date" in {
        def test(datesAndExpectedYears: Seq[(String, Int)]): Unit = {
          datesAndExpectedYears.foreach { case (date, expectedYear) =>
            withClue(s"Given $date:") {
              val result: TaxYear = TaxYear.fromIso(date)
              result.year shouldBe expectedYear
            }
          }
        }

        val input: List[(String, Int)] = List(
          "2025-01-01" -> 2025,
          "2025-04-01" -> 2025,
          "2025-04-06" -> 2026,
          "2023-06-01" -> 2024,
          "2026-01-01" -> 2026,
          "2021-12-31" -> 2022
        )

        test(input)
      }
    }

    "constructed from a downstream tax year" should {
      "return the downstream tax year" in {
        TaxYear.fromDownstream("2019").asDownstream shouldBe "2019"
      }

      "allow the MTD tax year to be extracted" in {
        TaxYear.fromDownstream("2019").asMtd shouldBe "2018-19"
      }
    }

    "constructed directly" should {
      "not compile" in {
        """new TaxYear("2021-22")""" shouldNot compile
      }
    }

    "compared with equals" should {
      "have equality based on content" in {
        val taxYear: TaxYear = TaxYear.fromMtd("2021-22")
        taxYear shouldBe TaxYear.fromDownstream("2022")
        taxYear should not be TaxYear.fromDownstream("2021")
      }
    }

    val requestJson: JsValue = Json.parse(""""2018-19"""")

    val model: TaxYear = TaxYear.fromMtd("2018-19")

    "written to JSON" should {
      "return the expected JsValue" in {
        Json.toJson(model) shouldBe requestJson
      }
    }

    ".now" should {
      "return the current tax year" in {
        val now: LocalDate = LocalDate.now(ZoneId.of("UTC"))
        val year: Int      = now.getYear

        val expectedYear: Int = {
          val taxYearStartDate: LocalDate = LocalDate.parse(s"$year-04-06")
          if (now.isBefore(taxYearStartDate)) year else year + 1
        }

        val result: TaxYear = TaxYear.now()
        result.year shouldBe expectedYear
      }
    }

    ".currentTaxYear" should {
      "return the current tax year" in {
        val today: LocalDate = LocalDate.now(ZoneId.of("UTC"))
        val year: Int        = today.getYear

        val expectedYear: Int = {
          val taxYearStartDate = LocalDate.parse(s"$year-04-06")
          if (today.isBefore(taxYearStartDate)) year else year + 1
        }

        val result = TaxYear.currentTaxYear()
        result.year shouldBe expectedYear
      }
    }
  }

}
