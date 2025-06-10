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

package v2.updateAccountingType

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveDetailedTaxYear, ResolveNino, ResolveNonEmptyJsonObject}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import config.AppConfig
import play.api.libs.json.JsValue
import v2.updateAccountingType.model.request._

class UpdateAccountingTypeValidator(nino: String, businessId: String, taxYear: String, body: JsValue)(implicit appConfig: AppConfig)
    extends Validator[UpdateAccountingTypeRequestData] {

  private val resolveJson: ResolveNonEmptyJsonObject[UpdateAccountingTypeRequestBody] =
    new ResolveNonEmptyJsonObject[UpdateAccountingTypeRequestBody]()

  private val resolveTaxYear: ResolveDetailedTaxYear = ResolveDetailedTaxYear(
    minimumTaxYear = TaxYear.ending(appConfig.accountingTypeMinimumTaxYear),
    allowIncompleteTaxYear = false
  )

  def validate: Validated[Seq[MtdError], UpdateAccountingTypeRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ) mapN UpdateAccountingTypeRequestData

}
