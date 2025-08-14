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

package v2.disapplyLateAccountingDateRule

import api.controllers.validators.{MockValidatorFactory, Validator}
import config.AppConfig
import org.scalamock.handlers.CallHandler
import v2.disapplyLateAccountingDateRule.model.request.DisapplyLateAccountingDateRuleRequest

trait MockDisapplyLateAccountingDateRuleValidatorFactory extends MockValidatorFactory[DisapplyLateAccountingDateRuleRequest] {

  val mockDisapplyLateAccountingDateRuleValidatorFactory: DisapplyLateAccountingDateRuleValidatorFactory =
    mock[DisapplyLateAccountingDateRuleValidatorFactory]

  def validator(): CallHandler[Validator[DisapplyLateAccountingDateRuleRequest]] = {
    (mockDisapplyLateAccountingDateRuleValidatorFactory
      .validator(_: String, _: String, _: String, _: Boolean)(_: AppConfig))
      .expects(*, *, *, *, *)
  }

}
