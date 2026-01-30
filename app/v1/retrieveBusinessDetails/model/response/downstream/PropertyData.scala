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

import api.models.domain.TypeOfBusiness
import play.api.libs.json.*
import utils.JsonTransformers.{conditionalCopy, conditionalUpdate}

case class PropertyData(
    incomeSourceType: Option[TypeOfBusiness],
    incomeSourceId: String,
    accountingPeriodStartDate: String,
    accountingPeriodEndDate: String,
    tradingStartDate: Option[String],
    cessationDate: Option[String],
    firstAccountingPeriodStartDate: Option[String],
    firstAccountingPeriodEndDate: Option[String],
    latencyDetails: Option[LatencyDetails],
    quarterTypeElection: Option[QuarterTypeElection]
)

object PropertyData {

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

    incomeSourceTypeTransformer
      .andThen(accountingPeriodTransformer)
      .andThen(tradingStartDateTransformer)
  }

  implicit val reads: Reads[PropertyData] = Json.reads.preprocess { jsValue =>
    jsValue.transform(combinedTransformer).getOrElse(jsValue)
  }

}
