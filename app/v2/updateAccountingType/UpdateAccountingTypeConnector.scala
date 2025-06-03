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

package v2.updateAccountingType

import api.connectors.DownstreamUri.HipUri
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.updateAccountingType.model.request.UpdateAccountingTypeRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpdateAccountingTypeConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def update(request: UpdateAccountingTypeRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String
  ): Future[DownstreamOutcome[Unit]] = {

    import request._

    val queryParams = Map(
      "taxYear" -> taxYear.asDownstream
    )

    val mappedQueryParams: Map[String, String] = queryParams.collect { case (k: String, v: String) => (k, v) }

    implicit val successCode: SuccessCode = SuccessCode(NO_CONTENT)

    val downstreamUri = HipUri[Unit](s"itsd/income-sources/$nino/accounting-type/$businessId")

    put(body, downstreamUri, mappedQueryParams.toList)

  }

}
