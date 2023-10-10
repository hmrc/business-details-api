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
import api.models.errors.{InternalError, MtdError, NinoFormatError, NotFoundError, RuleIncorrectGovTestScenarioError}
import api.models.outcomes.ResponseWrapper
import api.services.{BaseService, ServiceOutcome}
import cats.data.EitherT
import config.{AppConfig, FeatureSwitches}
import play.api.libs.json.Reads
import v1.connectors.RetrieveBusinessDetailsConnector
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRequestData
import v1.models.response.retrieveBusinessDetails.RetrieveBusinessDetailsResponse
import v1.models.response.retrieveBusinessDetails.downstream.RetrieveBusinessDetailsDownstreamResponse
import v1.models.response.retrieveBusinessDetails.downstream.RetrieveBusinessDetailsDownstreamResponse.getReads

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBusinessDetailsService @Inject() (connector: RetrieveBusinessDetailsConnector, appConfig: AppConfig) extends BaseService {

  def retrieveBusinessDetailsService(request: RetrieveBusinessDetailsRequestData)(implicit
                                                                                  ctx: RequestContext,
                                                                                  ec: ExecutionContext): Future[ServiceOutcome[RetrieveBusinessDetailsResponse]] = {

    val isIfsEnabled                                                             = FeatureSwitches()(appConfig).isIfsEnabled
    implicit val responseReads: Reads[RetrieveBusinessDetailsDownstreamResponse] = getReads(isIfsEnabled)

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieveBusinessDetails(request))
        .leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper <- EitherT
        .fromEither[Future](filterId(downstreamResponseWrapper, request.businessId))
        .flatMap(response => EitherT.fromEither[Future](retrieveAdditionalFieldsResponseMap(response)))
    } yield mtdResponseWrapper

    result.value
  }

  private def retrieveAdditionalFieldsResponseMap(
      responseWrapper: ResponseWrapper[RetrieveBusinessDetailsResponse]): ServiceOutcome[RetrieveBusinessDetailsResponse] = {
    val response = responseWrapper.responseData

    if (FeatureSwitches()(appConfig).isRetrieveAdditionalFieldsEnabled) {
      Right(responseWrapper.copy(responseData = response.addRetrieveAdditionalFields.reformatLatencyDetailsTaxYears))
    } else {
      Right(responseWrapper.copy(responseData = response))
    }
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_NINO"         -> NinoFormatError,
      "INVALID_MTDBSA"       -> InternalError,
      "UNMATCHED_STUB_ERROR" -> RuleIncorrectGovTestScenarioError,
      "NOT_FOUND_NINO"       -> NotFoundError,
      "NOT_FOUND_MTDBSA"     -> InternalError,
      "SERVER_ERROR"         -> InternalError,
      "SERVICE_UNAVAILABLE"  -> InternalError
    )

    val extraIfsErrors = Map(
      "INVALID_MTD_ID"        -> InternalError,
      "INVALID_CORRELATIONID" -> InternalError,
      "INVALID_IDTYPE"        -> InternalError,
      "NOT_FOUND"             -> NotFoundError
    )
    errors ++ extraIfsErrors
  }

}
