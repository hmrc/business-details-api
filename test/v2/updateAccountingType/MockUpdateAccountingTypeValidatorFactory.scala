/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.updateAccountingType

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import config.{AppConfig, MockAppConfig}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import v2.updateAccountingType.model.request.UpdateAccountingTypeRequestData

trait MockUpdateAccountingTypeValidatorFactory extends MockFactory with MockAppConfig {

  val mockUpdateAccountingTypeValidatorFactory: UpdateAccountingTypeValidatorFactory =
    mock[UpdateAccountingTypeValidatorFactory]

  object MockedUpdateAccountingTypeValidatorFactory {

    def expectValidator(): CallHandler[Validator[UpdateAccountingTypeRequestData]] = {
      (mockUpdateAccountingTypeValidatorFactory
        .validator(_: String, _: String, _: String, _: JsValue)(_: AppConfig))
        .expects(*, *, *, *, *)
    }

  }

  def willUseValidator(use: Validator[UpdateAccountingTypeRequestData]): CallHandler[Validator[UpdateAccountingTypeRequestData]] = {
    MockedUpdateAccountingTypeValidatorFactory
      .expectValidator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: UpdateAccountingTypeRequestData): Validator[UpdateAccountingTypeRequestData] =
    new Validator[UpdateAccountingTypeRequestData] {
      def validate: Validated[Seq[MtdError], UpdateAccountingTypeRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[UpdateAccountingTypeRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[UpdateAccountingTypeRequestData] =
    new Validator[UpdateAccountingTypeRequestData] {
      def validate: Validated[Seq[MtdError], UpdateAccountingTypeRequestData] = Invalid(result)
    }

}
