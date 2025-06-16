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

package v2.createUpdatePeriodsOfAccount

import api.connectors.DownstreamUri.HipUri
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequest
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateUpdatePeriodsOfAccountConnector @Inject() (val httpClientV2: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createUpdate(request: CreateUpdatePeriodsOfAccountRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    val downstreamUri: DownstreamUri[Unit] =
      HipUri[Unit](s"itsd/income-sources/$nino/periods-of-account/$businessId?taxYear=${taxYear.asTysDownstream}")

    put(body, downstreamUri)
  }

}
