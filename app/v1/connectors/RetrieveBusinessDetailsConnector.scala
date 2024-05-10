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

package v1.connectors

import api.connectors.DownstreamUri.{DesUri, IfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.domain.Nino
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.response.downstream.retrieveBusinessDetails.RetrieveBusinessDetailsDownstreamResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveBusinessDetailsConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveBusinessDetails(nino: Nino)(implicit
                                             hc: HeaderCarrier,
                                             ec: ExecutionContext,
                                             correlationId: String): Future[DownstreamOutcome[RetrieveBusinessDetailsDownstreamResponse]] = {

    val downstreamUri = s"registration/business-details/nino/$nino"

    if (featureSwitches.isIfsEnabled) {
      get(IfsUri[RetrieveBusinessDetailsDownstreamResponse](downstreamUri))
    } else {
      get(DesUri[RetrieveBusinessDetailsDownstreamResponse](downstreamUri))
    }
  }

}
