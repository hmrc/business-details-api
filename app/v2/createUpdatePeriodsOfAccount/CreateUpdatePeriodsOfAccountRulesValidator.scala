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

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.ResolveDateRange
import api.models.domain.{DateRange, TaxYear}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import v2.common.models.PeriodsOfAccountDates
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequest

object CreateUpdatePeriodsOfAccountRulesValidator extends RulesValidator[CreateUpdatePeriodsOfAccountRequest] {

  override def validateBusinessRules(
      parsed: CreateUpdatePeriodsOfAccountRequest
  ): Validated[Seq[MtdError], CreateUpdatePeriodsOfAccountRequest] = {
    import parsed._

    combine(
      validatePeriodsOfAccount(body.periodsOfAccount, body.periodsOfAccountDates),
      validateDates(body.periodsOfAccountDates, taxYear)
    ).onSuccess(parsed)
  }

  private def validatePeriodsOfAccount(periodsOfAccount: Boolean,
                                       periodsOfAccountDates: Option[Seq[PeriodsOfAccountDates]]): Validated[Seq[MtdError], Unit] = {
    val isValid: Boolean = (periodsOfAccount && periodsOfAccountDates.nonEmpty) || (!periodsOfAccount && periodsOfAccountDates.isEmpty)
    if (isValid) valid else Invalid(List(RulePeriodsOfAccountError))
  }

  private def validateDates(periodsOfAccountDates: Option[Seq[PeriodsOfAccountDates]], taxYear: TaxYear): Validated[Seq[MtdError], Unit] = {

    val resolveDateRanges: Validated[Seq[MtdError], Seq[DateRange]] =
      periodsOfAccountDates
        .getOrElse(Seq.empty)
        .zipWithIndex
        .traverse { case (periodOfAccountDates, index) =>
          val basePath: String      = s"/periodsOfAccountDates/$index"
          val startDatePath: String = s"$basePath/startDate"
          val endDatePath: String   = s"$basePath/endDate"

          ResolveDateRange(
            startDateFormatError = StartDateFormatError.withPath(startDatePath),
            endDateFormatError = EndDateFormatError.withPath(endDatePath),
            endBeforeStartDateError = RuleEndBeforeStartDateError.withPath(basePath)
          ).withDatesLimitedTo(
            minDate = taxYear.startDate,
            minError = RuleStartDateError.withPath(startDatePath),
            maxDate = taxYear.endDate,
            maxError = RuleEndDateError.withPath(endDatePath),
            enforceStartOnOrAfterMin = false
          )(periodOfAccountDates.startDate -> periodOfAccountDates.endDate)
        }

    resolveDateRanges.andThen { dateRanges =>
      val hasOverlap: Boolean = dateRanges.tails.exists {
        case currentPeriod +: remainingPeriods =>
          remainingPeriods.exists { nextPeriod =>
            !(currentPeriod.endDate.isBefore(nextPeriod.startDate) || nextPeriod.endDate.isBefore(currentPeriod.startDate))
          }
        case _ => false
      }

      if (hasOverlap) Invalid(List(RulePeriodsOverlapError.withPath("/periodsOfAccountDates"))) else valid
    }
  }

}
