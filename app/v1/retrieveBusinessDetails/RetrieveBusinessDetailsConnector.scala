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

package v1.retrieveBusinessDetails

import api.connectors.DownstreamUri.{DesUri, HipUri, IfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.domain.Nino
import config.AppConfig
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateUtils.nowAsUtc
import v1.retrieveBusinessDetails.model.response.downstream.RetrieveBusinessDetailsDownstreamResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveBusinessDetailsConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveBusinessDetails(nino: Nino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveBusinessDetailsDownstreamResponse]] = {

    val desIfsDownstreamUri: String = s"registration/business-details/nino/$nino"
    val hipDownstreamUri: String    = s"etmp/RESTAdapter/itsa/taxpayer/business-details?nino=$nino"

    val additionalContractHeaders: Seq[(String, String)] = List(
      "X-Message-Type"        -> "TaxpayerDisplay",
      "X-Originating-System"  -> "MDTP",
      "X-Receipt-Date"        -> nowAsUtc,
      "X-Regime-Type"         -> "ITSA",
      "X-Transmitting-System" -> "HIP"
    )

    if (featureSwitches.isEnabled("ifs_hip_migration_1171")) {
      get(HipUri[RetrieveBusinessDetailsDownstreamResponse](hipDownstreamUri, additionalContractHeaders))
    } else if (featureSwitches.isIfsEnabled) {
      get(IfsUri[RetrieveBusinessDetailsDownstreamResponse](desIfsDownstreamUri))
    } else {
      get(DesUri[RetrieveBusinessDetailsDownstreamResponse](desIfsDownstreamUri))
    }
  }

}
