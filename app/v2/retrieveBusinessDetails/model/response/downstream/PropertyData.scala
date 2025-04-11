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

package v2.retrieveBusinessDetails.model.response.downstream

import api.models.domain.{AccountingType, TypeOfBusiness}
import play.api.libs.json.{JsObject, JsPath, JsString, Json, Reads}
import play.api.libs.json.Reads.of

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

  private val desToIfsCashOrAccrualsTransformer =
    JsPath.json.update((JsPath \ "cashOrAccruals").json.copyFrom((JsPath \ "cashOrAccrualsFlag").json.pick))

  private val ifsToHipAccountingPeriodStartDateTransformer =
    JsPath.json.update((JsPath \ "accountingPeriodStartDate").json.copyFrom((JsPath \ "accPeriodSDate").json.pick))

  private val ifsToHipAccountingPeriodEndDateTransformer =
    JsPath.json.update((JsPath \ "accountingPeriodEndDate").json.copyFrom((JsPath \ "accPeriodEDate").json.pick))

  private val ifsToHipTradingStartDateTransformer =
    JsPath.json.update((JsPath \ "tradingStartDate").json.copyFrom((JsPath \ "tradingSDate").json.pick))

  private val ifsToHipincomeSourceTypeTransformer = (JsPath \ "incomeSourceType").json
    .update(
      of[JsString].map {
        case JsString("02") => JsString("uk-property")
        case JsString("03") => JsString("foreign-property")
        case other          => other
      }
    )
    .orElse(JsPath.json.pick[JsObject])

  implicit val reads: Reads[PropertyData] = Json.reads
    .preprocess(jsValue => jsValue.transform(desToIfsCashOrAccrualsTransformer).getOrElse(jsValue))
    .preprocess(jsValue => jsValue.transform(ifsToHipincomeSourceTypeTransformer).getOrElse(jsValue))
    .preprocess(jsValue => jsValue.transform(ifsToHipAccountingPeriodStartDateTransformer).getOrElse(jsValue))
    .preprocess(jsValue => jsValue.transform(ifsToHipAccountingPeriodEndDateTransformer).getOrElse(jsValue))
    .preprocess(jsValue => jsValue.transform(ifsToHipTradingStartDateTransformer).getOrElse(jsValue))

}
