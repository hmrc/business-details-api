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
import play.api.libs.json.JsValue
import v2.updateAccountingType.def1.Def1_UpdateAccountingTypeValidator
import v2.updateAccountingType.model.request.UpdateAccountingTypeRequestData

import javax.inject.Singleton

@Singleton
class UpdateAccountingTypeValidatorFactory {

  def validator(nino: String, businessId: String, taxYear: String, body: JsValue): Validator[UpdateAccountingTypeRequestData] =
    new Def1_UpdateAccountingTypeValidator(nino, businessId, taxYear, body)

}
