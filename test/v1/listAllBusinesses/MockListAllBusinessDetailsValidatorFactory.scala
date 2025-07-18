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

package v1.listAllBusinesses

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v1.listAllBusinesses.model.request.ListAllBusinessesRequestData

trait MockListAllBusinessDetailsValidatorFactory extends TestSuite with MockFactory {

  val mockListAllBusinessDetailsValidatorFactory: ListAllBusinessDetailsValidatorFactory = mock[ListAllBusinessDetailsValidatorFactory]

  object MockedListAllBusinessDetailsValidatorFactory {

    def expectValidator(): CallHandler[Validator[ListAllBusinessesRequestData]] = {
      (mockListAllBusinessDetailsValidatorFactory
        .validator(_: String))
        .expects(*)
    }

  }

  def willUseValidator(use: Validator[ListAllBusinessesRequestData]): CallHandler[Validator[ListAllBusinessesRequestData]] = {
    MockedListAllBusinessDetailsValidatorFactory
      .expectValidator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: ListAllBusinessesRequestData): Validator[ListAllBusinessesRequestData] =
    new Validator[ListAllBusinessesRequestData] {
      def validate: Validated[Seq[MtdError], ListAllBusinessesRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[ListAllBusinessesRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[ListAllBusinessesRequestData] =
    new Validator[ListAllBusinessesRequestData] {
      def validate: Validated[Seq[MtdError], ListAllBusinessesRequestData] = Invalid(result)
    }

}
