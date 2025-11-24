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

import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._

import scala.math.Ordered.orderingToOrdered

/** Provides utilities and extension methods for resolvers and validators.
  */
trait ResolverSupport {
  type Resolver[In, Out] = In => Validated[Seq[MtdError], Out]
  type Validator[A]      = A => Option[Seq[MtdError]]

  implicit class ResolverOps[In, Out](resolver: In => Validated[Seq[MtdError], Out]) {
    def thenValidate(validator: Validator[Out]): Resolver[In, Out] = i => resolver(i).andThen(o => validator(o).toInvalid(o))
  }

  def satisfies[A](error: => MtdError)(predicate: A => Boolean): Validator[A] =
    a => Option.when(!predicate(a))(List(error))

  def satisfiesMin[A: Ordering](minAllowed: A, error: => MtdError): Validator[A] = satisfies(error)(minAllowed <= _)

  def combinedValidator[A](first: Validator[A], others: Validator[A]*): Validator[A] = { (value: A) =>
    val validators = first +: others

    val validations = validators.map(validator => validator(value))

    validations.reduce(_ combine _)
  }

}

/** To allow an import-based alternative to extension */
object ResolverSupport extends ResolverSupport
