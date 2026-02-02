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

package v1.retrieveBusinessDetails.model.response.downstream

import play.api.libs.json.{JsObject, JsPath, Json, Reads}
import utils.JsonTransformers.conditionalCopy

case class BusinessData(
    incomeSourceId: String,
    accountingPeriodStartDate: String,
    accountingPeriodEndDate: String,
    tradingName: Option[String],
    businessAddressDetails: Option[BusinessAddressDetails],
    tradingStartDate: Option[String],
    cessationDate: Option[String],
    firstAccountingPeriodStartDate: Option[String],
    firstAccountingPeriodEndDate: Option[String],
    latencyDetails: Option[LatencyDetails],
    quarterTypeElection: Option[QuarterTypeElection]
)

object BusinessData {

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

    accountingPeriodTransformer
      .andThen(tradingStartDateTransformer)
  }

  implicit val reads: Reads[BusinessData] = Json.reads.preprocess { jsValue =>
    jsValue.transform(combinedTransformer).getOrElse(jsValue)
  }

}
