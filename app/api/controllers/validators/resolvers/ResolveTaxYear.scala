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

import java.time.Clock
import scala.math.Ordering.Implicits.infixOrderingOps

object ResolveTaxYear extends ResolverSupport {

  private val taxYearFormat = "20([1-9][0-9])-([1-9][0-9])".r

  val resolver: Resolver[String, TaxYear] = {
    case value @ taxYearFormat(start, end) =>
      if (end.toInt - start.toInt == 1) {
        Valid(TaxYear.fromMtd(value))
      } else {
        Invalid(List(RuleTaxYearRangeInvalidError))
      }

    case _ => Invalid(List(TaxYearFormatError))
  }

  def resolverWithCustomErrors(formatError: MtdError, rangeError: MtdError): Resolver[String, TaxYear] = {
    case value @ taxYearFormat(start, end) =>
      if (end.toInt - start.toInt == 1) {
        Valid(TaxYear.fromMtd(value))
      } else {
        Invalid(List(rangeError))
      }

    case _ => Invalid(List(formatError))
  }

  def apply(value: String): Validated[Seq[MtdError], TaxYear] = resolver(value)
}

case class ResolveDetailedTaxYear(minimumTaxYear: TaxYear,
                                  notSupportedError: MtdError = RuleTaxYearNotSupportedError,
                                  allowIncompleteTaxYear: Boolean = true,
                                  incompleteTaxYearError: MtdError = RuleTaxYearNotEndedError,
                                  formatError: MtdError = TaxYearFormatError,
                                  rangeError: MtdError = RuleTaxYearRangeInvalidError)(implicit clock: Clock = Clock.systemUTC)
    extends ResolverSupport {

  private val baseResolver: Resolver[String, TaxYear] = ResolveTaxYear.resolverWithCustomErrors(formatError, rangeError)

  private val withMinCheck: Resolver[String, TaxYear] = baseResolver thenValidate satisfiesMin(minimumTaxYear, notSupportedError)

  private val fullResolver: Resolver[String, TaxYear] =
    if (allowIncompleteTaxYear) {
      withMinCheck
    } else {
      withMinCheck thenValidate satisfies(incompleteTaxYearError)(_ < TaxYear.currentTaxYear)
    }

  def apply(value: String): Validated[Seq[MtdError], TaxYear] = fullResolver(value)
}
