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

package v1.services

import api.controllers.RequestContext
import api.models.errors.{InternalError, MtdError, NinoFormatError, NotFoundError}
import api.services.{BaseService, ServiceOutcome}
import cats.data.EitherT
import config.{AppConfig, FeatureSwitches}

import javax.inject.{Inject, Singleton}
import v1.connectors.RetrieveBusinessDetailsConnector
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRequest
import v1.models.response.retrieveBusinessDetails.RetrieveBusinessDetailsResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBusinessDetailsService @Inject() (connector: RetrieveBusinessDetailsConnector) extends BaseService {

  def retrieveBusinessDetailsService(request: RetrieveBusinessDetailsRequest, appConfig: AppConfig)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveBusinessDetailsResponse]] = {

    val r10AdditionalFieldsEnabled = FeatureSwitches()(appConfig).r10AdditionalFieldsEnabled

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieveBusinessDetails(request))
        .leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper <- EitherT.fromEither[Future](filterId(downstreamResponseWrapper, request.businessId, r10AdditionalFieldsEnabled))
    } yield mtdResponseWrapper

    result.value
  }

  private val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"        -> NinoFormatError,
      "INVALID_MTDBSA"      -> InternalError,
      "NOT_FOUND_NINO"      -> NotFoundError,
      "NOT_FOUND_MTDBSA"    -> InternalError,
      "SERVER_ERROR"        -> InternalError,
      "SERVICE_UNAVAILABLE" -> InternalError
    )

}
