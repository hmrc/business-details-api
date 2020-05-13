/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import v1.models.errors.{BusinessIdFormatError, MtdError}

object BusinessIdValidation {

  private val businessIdRegex = "^X[a-zA-Z0-9]{1}IS[0-9]{12}$"

  def validate(businessId: String): List[MtdError] = {
    if (businessId.matches(businessIdRegex)) NoValidationErrors else List(BusinessIdFormatError)
  }
}
