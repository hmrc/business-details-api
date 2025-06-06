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

package v2.retrieveAccountingType

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{DetailedResolveTaxYear, ResolveBusinessId, ResolveNino}
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated._
import cats.implicits._
import config.AppConfig
import v2.retrieveAccountingType.model.request.RetrieveAccountingTypeRequest

class RetrieveAccountingTypeValidator(nino: String, businessId: String, taxYear: String)(implicit appConfig: AppConfig)
    extends Validator[RetrieveAccountingTypeRequest] {

  private val resolveTaxYear =
    DetailedResolveTaxYear(allowIncompleteTaxYear = false, maybeMinimumTaxYear = Some(appConfig.accountingTypeMinimumTaxYear))

  def validate: Validated[Seq[MtdError], RetrieveAccountingTypeRequest] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveTaxYear(taxYear)
    ).mapN(RetrieveAccountingTypeRequest)

}
