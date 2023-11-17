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

package api.models.errors

import play.api.http.Status._

object RuleIncomeSourceNotFoundError extends MtdError("RULE_INCOME_SOURCE_NOT_FOUND", "The business ID provided is not found", NOT_FOUND)

object RuleIncomeSourceStateConflictError
    extends MtdError("RULE_INCOME_SOURCE_STATE_CONFLICT", "The request conflicts with the current state of the income source", BAD_REQUEST)

object RuleQuarterlyPeriodUpdatingError
    extends MtdError(
      "RULE_QUARTERLY_PERIOD_UPDATING",
      "Quarterly period type cannot be changed for the current year as the business is treated as commencing in the following year",
      BAD_REQUEST)
