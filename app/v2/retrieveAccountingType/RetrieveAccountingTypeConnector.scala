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

import api.connectors.DownstreamUri.HipUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import v2.retrieveAccountingType.model.request.RetrieveAccountingTypeRequest
import v2.retrieveAccountingType.model.response.RetrieveAccountingTypeResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveAccountingTypeConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveAccountingType(request: RetrieveAccountingTypeRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveAccountingTypeResponse]] = {

    val downstreamUri: String = s"itsd/income-sources/v2/${request.nino}"

    val queryParams = Map(
      "incomeSourceId"  -> request.businessId.toString,
      "taxYearExplicit" -> request.taxYear.asMtd
    )

    val mappedQueryParams: Map[String, String] = queryParams.collect { case (k: String, v: String) => (k, v) }

    val maybeIntent: Option[String] = if (featureSwitches.isEnabled("passIntentHeader")) Some("ACCOUNTING_TYPE") else None

    get(HipUri[RetrieveAccountingTypeResponse](downstreamUri), mappedQueryParams.toList, maybeIntent)

  }

}
