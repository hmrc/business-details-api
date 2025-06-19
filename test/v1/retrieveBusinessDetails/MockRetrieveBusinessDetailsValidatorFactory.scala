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

package v1.retrieveBusinessDetails

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v1.retrieveBusinessDetails.model.request.RetrieveBusinessDetailsRequestData

trait MockRetrieveBusinessDetailsValidatorFactory extends TestSuite with MockFactory {

  val mockRetrieveBusinessDetailsValidatorFactory: RetrieveBusinessDetailsValidatorFactory = mock[RetrieveBusinessDetailsValidatorFactory]

  object MockedRetrieveBusinessDetailsValidatorFactory {

    def expectValidator(): CallHandler[Validator[RetrieveBusinessDetailsRequestData]] = {
      (mockRetrieveBusinessDetailsValidatorFactory
        .validator(_: String, _: String))
        .expects(*, *)
    }

  }

  def willUseValidator(use: Validator[RetrieveBusinessDetailsRequestData]): CallHandler[Validator[RetrieveBusinessDetailsRequestData]] = {
    MockedRetrieveBusinessDetailsValidatorFactory
      .expectValidator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveBusinessDetailsRequestData): Validator[RetrieveBusinessDetailsRequestData] =
    new Validator[RetrieveBusinessDetailsRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveBusinessDetailsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveBusinessDetailsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveBusinessDetailsRequestData] =
    new Validator[RetrieveBusinessDetailsRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveBusinessDetailsRequestData] = Invalid(result)
    }

}
