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

package v2.createAmendQuarterlyPeriodType

import api.connectors.DownstreamUri.{Api2089Uri, HipUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import play.api.http.Status.OK
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.createAmendQuarterlyPeriodType.model.request.CreateAmendQuarterlyPeriodTypeRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendQuarterlyPeriodTypeConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def create(request: CreateAmendQuarterlyPeriodTypeRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String
  ): Future[DownstreamOutcome[Unit]] = {

    import request._

    implicit val successCode: SuccessCode = SuccessCode(OK)

    val downstreamUri =
      if (featureSwitches.isEnabled("ifs_hip_migration_2089")) {
        HipUri[Unit](s"itsd/income-sources/reporting-type/$nino/$businessId?taxYear=${taxYear.asTysDownstream}")
      } else {
        Api2089Uri[Unit](s"income-tax/${taxYear.asTysDownstream}/income-sources/reporting-type/$nino/$businessId")
      }

    put(body, downstreamUri)

  }

}
