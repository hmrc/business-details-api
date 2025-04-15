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

import api.models.domain.AccountingType
import play.api.libs.json.{JsObject, JsPath, Json, Reads}
import utils.JsonTransformers.conditionalCopy

case class BusinessData(
    incomeSourceId: String,
    accountingPeriodStartDate: String,
    accountingPeriodEndDate: String,
    tradingName: Option[String],
    businessAddressDetails: Option[BusinessAddressDetails],
    tradingStartDate: Option[String],
    cashOrAccruals: Option[AccountingType],
    cessationDate: Option[String],
    firstAccountingPeriodStartDate: Option[String],
    firstAccountingPeriodEndDate: Option[String],
    latencyDetails: Option[LatencyDetails],
    quarterTypeElection: Option[QuarterTypeElection]
)

object BusinessData {

  private implicit val acctTypeReads: Reads[AccountingType] = {
    val readBoolean: Reads[AccountingType] = JsPath.read[Boolean].map { flag =>
      if (flag) AccountingType.ACCRUALS else AccountingType.CASH
    }

    val readString: Reads[AccountingType] = JsPath.read[String].map {
      case "cash"     => AccountingType.CASH
      case "accruals" => AccountingType.ACCRUALS
      case other      => throw new RuntimeException(s"Unexpected cashOrAccruals '$other'")
    }

    readBoolean orElse readString
  }

  private val combinedTransformer: Reads[JsObject] = {

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

    accountingPeriodTransformer
      .andThen(tradingStartDateTransformer)
      .andThen(cashOrAccrualsTransformer)
  }

  implicit val reads: Reads[BusinessData] = Json.reads.preprocess { jsValue =>
    jsValue.transform(combinedTransformer).getOrElse(jsValue)
  }

}
