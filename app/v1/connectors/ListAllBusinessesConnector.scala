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

import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.connectors.DownstreamUri.{DesUri, IfsUri}
import config.AppConfig

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.listAllBusinesses.ListAllBusinessesRequest
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesResponse}
import api.connectors.httpparsers.StandardDownstreamHttpParser._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListAllBusinessesConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def listAllBusinesses(request: ListAllBusinessesRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[ListAllBusinessesResponse[Business]]] = {

    import request._

    val downstreamUri = s"registration/business-details/nino/$nino"

    if (featureSwitches.r10IFSEnabled) {
      get(IfsUri[ListAllBusinessesResponse[Business]](downstreamUri))
    } else {
      get(DesUri[ListAllBusinessesResponse[Business]](downstreamUri))
    }

  }

}
