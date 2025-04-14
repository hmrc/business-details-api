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

package v2.retrieveBusinessDetails.model.response.downstream

import api.models.domain.{AccountingType, TypeOfBusiness}
import play.api.libs.json.{JsObject, JsPath, JsString, Json, Reads}
import utils.JsonTransformers.{conditionalCopy, conditionalUpdate}

case class PropertyData(
    incomeSourceType: Option[TypeOfBusiness],
    incomeSourceId: String,
    accountingPeriodStartDate: String,
    accountingPeriodEndDate: String,
    tradingStartDate: Option[String],
    cashOrAccruals: Option[AccountingType],
    cessationDate: Option[String],
    firstAccountingPeriodStartDate: Option[String],
    firstAccountingPeriodEndDate: Option[String],
    latencyDetails: Option[LatencyDetails],
    quarterTypeElection: Option[QuarterTypeElection]
)

object PropertyData {

  private implicit val acctTypeReads: Reads[AccountingType] =
    JsPath.read[Boolean].map { flag =>
      if (flag) AccountingType.ACCRUALS else AccountingType.CASH
    }

  private val combinedTransformer: Reads[JsObject] = {

    val incomeSourceTypeTransformer: Reads[JsObject] = conditionalUpdate(
      path = JsPath \ "incomeSourceType",
      transform = {
        case JsString("02") => JsString("uk-property")
        case JsString("03") => JsString("foreign-property")
        case other          => other
      }
    )

    val accountingPeriodTransformer: Reads[JsObject] = conditionalCopy(
      sourcePath = JsPath \ "accPeriodSDate",
      targetPath = JsPath \ "accountingPeriodStartDate"
    ).andThen(
      conditionalCopy(
        sourcePath = JsPath \ "accPeriodEDate",
        targetPath = JsPath \ "accountingPeriodEndDate"
      )
    )

    val tradingStartDateTransformer: Reads[JsObject] = conditionalCopy(
      sourcePath = JsPath \ "tradingSDate",
      targetPath = JsPath \ "tradingStartDate"
    )

    val cashOrAccrualsTransformer: Reads[JsObject] = conditionalCopy(
      sourcePath = JsPath \ "cashOrAccrualsFlag",
      targetPath = JsPath \ "cashOrAccruals"
    )

    incomeSourceTypeTransformer
      .andThen(accountingPeriodTransformer)
      .andThen(tradingStartDateTransformer)
      .andThen(cashOrAccrualsTransformer)
  }

  implicit val reads: Reads[PropertyData] = Json.reads.preprocess { jsValue =>
    jsValue.transform(combinedTransformer).getOrElse(jsValue)
  }

}
