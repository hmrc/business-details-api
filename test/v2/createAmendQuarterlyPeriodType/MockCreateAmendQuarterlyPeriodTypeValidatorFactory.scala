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

package v2.createAmendQuarterlyPeriodType

import api.controllers.validators.{MockValidatorFactory, Validator}
import org.scalamock.handlers.CallHandler
import play.api.libs.json.JsValue
import v2.createAmendQuarterlyPeriodType.model.request.CreateAmendQuarterlyPeriodTypeRequestData

trait MockCreateAmendQuarterlyPeriodTypeValidatorFactory extends MockValidatorFactory[CreateAmendQuarterlyPeriodTypeRequestData] {

  val mockCreateAmendQuarterlyPeriodTypeValidatorFactory: CreateAmendQuarterlyPeriodTypeValidatorFactory =
    mock[CreateAmendQuarterlyPeriodTypeValidatorFactory]

  def validator(): CallHandler[Validator[CreateAmendQuarterlyPeriodTypeRequestData]] = {
    (mockCreateAmendQuarterlyPeriodTypeValidatorFactory
      .validator(_: String, _: String, _: String, _: JsValue))
      .expects(*, *, *, *)
  }

}
