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

package api.models.errors

import play.api.http.Status._

object RuleBusinessIdNotFoundError extends MtdError("RULE_BUSINESS_ID_NOT_FOUND", "The business ID provided is not found", NOT_FOUND)

object RuleBusinessIdStateConflictError
    extends MtdError("RULE_BUSINESS_ID_STATE_CONFLICT", "The request conflicts with the current state of the business ID", BAD_REQUEST)

object RulePeriodsOfAccountError
    extends MtdError(
      "RULE_PERIODS_OF_ACCOUNT",
      "Periods of account dates must be supplied if periodsOfAccount is set to true. They should not be supplied of periodsOfAccount is set to false",
      BAD_REQUEST
    )

object RuleEndBeforeStartDateError
    extends MtdError(
      "RULE_END_DATE_BEFORE_START_DATE",
      "The supplied values for periods of account endDate must not be earlier than the startDate",
      BAD_REQUEST
    )

object RuleStartDateError
    extends MtdError(
      "RULE_START_DATE",
      "One or more of the supplied periods of account start dates do not fall within or before the tax year provided",
      BAD_REQUEST
    )

object RuleEndDateError
    extends MtdError(
      "RULE_END_DATE",
      "One or more of the supplied periods of account end dates do not fall within the tax year provided",
      BAD_REQUEST
    )

object RulePeriodsOverlapError
    extends MtdError(
      "RULE_PERIODS_OVERLAP",
      "One or more of the supplied periods of account start and end dates overlap",
      BAD_REQUEST
    )

object RuleCessationDateError
    extends MtdError(
      "RULE_CESSATION_DATE",
      "One or more of the supplied periods of account end dates exceeds the business cessation date",
      BAD_REQUEST
    )

object RuleOutsideAmendmentWindowError
    extends MtdError(
      "RULE_OUTSIDE_AMENDMENT_WINDOW",
      "You are outside the amendment window",
      BAD_REQUEST
    )

object RuleQuarterlyPeriodUpdatingError
    extends MtdError(
      "RULE_QUARTERLY_PERIOD_UPDATING",
      "Quarterly period type cannot be changed for the current year as the business is treated as commencing in the following year",
      BAD_REQUEST
    )

object RuleNoAccountingDateFoundError
    extends MtdError(
      "RULE_NO_ACCOUNTING_DATE_FOUND",
      "Cannot disapply LADR. No Accounting Date found between 31 March and 4 April inclusive",
      UNPROCESSABLE_ENTITY
    )

object RuleElectionPeriodNotExpiredError
    extends MtdError(
      "RULE_ELECTION_PERIOD_NOT_EXPIRED",
      "Cannot change LADR disapplication. Existing status has not expired",
      UNPROCESSABLE_ENTITY
    )

object RuleTypeOfBusinessIncorrectError
    extends MtdError(
      "RULE_TYPE_OF_BUSINESS_INCORRECT",
      "The businessId is not for a self-employment business",
      UNPROCESSABLE_ENTITY
    )
