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

package v2.listAllBusinesses

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.ResolveNino
import api.models.errors.MtdError
import cats.data.Validated
import v2.listAllBusinesses.model.request.ListAllBusinessesRequestData

import javax.inject.Singleton

@Singleton
class ListAllBusinessDetailsValidatorFactory {

  def validator(nino: String): Validator[ListAllBusinessesRequestData] =
    new Validator[ListAllBusinessesRequestData] {

      def validate: Validated[Seq[MtdError], ListAllBusinessesRequestData] =
        ResolveNino(nino).map(ListAllBusinessesRequestData)

    }

}
