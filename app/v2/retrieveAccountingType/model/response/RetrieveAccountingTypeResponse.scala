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

package v2.retrieveAccountingType.model.response

import play.api.libs.json.{JsPath, OFormat}
import v2.common.models.AccountingType

case class RetrieveAccountingTypeResponse(accountingType: AccountingType)

object RetrieveAccountingTypeResponse {

  implicit val format: OFormat[RetrieveAccountingTypeResponse] = OFormat(
    (JsPath \\ "accountingType").read[AccountingType].map(RetrieveAccountingTypeResponse.apply),
    (JsPath \ "accountingType").write[AccountingType].contramap[RetrieveAccountingTypeResponse](_.accountingType)
  )

}
