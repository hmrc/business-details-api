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

package v1.retrieveBusinessDetails

import api.controllers.RequestContext
import api.models.domain.BusinessId
import api.models.errors.{
  ErrorWrapper,
  InternalError,
  MtdError,
  NinoFormatError,
  NoBusinessFoundError,
  NotFoundError,
  RuleIncorrectGovTestScenarioError
}
import api.models.outcomes.ResponseWrapper
import api.services.{BaseService, ServiceOutcome}
import cats.data.EitherT
import config.FeatureSwitches
import v1.retrieveBusinessDetails.model.request.RetrieveBusinessDetailsRequestData
import v1.retrieveBusinessDetails.model.response.RetrieveBusinessDetailsResponse
import v1.retrieveBusinessDetails.model.response.downstream.RetrieveBusinessDetailsDownstreamResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBusinessDetailsService @Inject() (connector: RetrieveBusinessDetailsConnector)(implicit featureSwitches: FeatureSwitches)
    extends BaseService {

  def retrieveBusinessDetailsService(request: RetrieveBusinessDetailsRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveBusinessDetailsResponse]] = {

    val result = for {
      downstreamResponseWrapper    <- EitherT(connector.retrieveBusinessDetails(request.nino)).leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper           <- EitherT.fromEither[Future](filterIdAndConvert(downstreamResponseWrapper, request.businessId))
      maybeWithQuarterlyTypeChoice <- EitherT.fromEither[Future](featureSwitchQuarterlyTypeChoice(mtdResponseWrapper))
    } yield maybeWithQuarterlyTypeChoice

    result.value
  }

  private def featureSwitchQuarterlyTypeChoice(
      responseWrapper: ResponseWrapper[RetrieveBusinessDetailsResponse]): Either[ErrorWrapper, ResponseWrapper[RetrieveBusinessDetailsResponse]] =
    if (featureSwitches.isScp005aQuarterlyTypeChoiceEnabled) Right(responseWrapper)
    else Right(responseWrapper.copy(responseData = responseWrapper.responseData.copy(quarterlyTypeChoice = None)))

  private def filterIdAndConvert(
      responseWrapper: ResponseWrapper[RetrieveBusinessDetailsDownstreamResponse],
      businessId: BusinessId
  ): Either[ErrorWrapper, ResponseWrapper[RetrieveBusinessDetailsResponse]] = {
    val downstreamResponse = responseWrapper.responseData

    val matchingBusinesses =
      downstreamResponse.businessData
        .getOrElse(Nil)
        .filter(_.incomeSourceId == businessId.businessId)
        .map(RetrieveBusinessDetailsResponse.fromBusinessData(_, downstreamResponse.yearOfMigration))

    val matchingProperties =
      downstreamResponse.propertyData
        .getOrElse(Nil)
        .filter(_.incomeSourceId == businessId.businessId)
        .map(RetrieveBusinessDetailsResponse.fromPropertyData(_, downstreamResponse.yearOfMigration))

    matchingBusinesses ++ matchingProperties match {
      case matchingData +: Seq() => Right(responseWrapper.map(_ => matchingData))
      case Nil                   => Left(ErrorWrapper(responseWrapper.correlationId, NoBusinessFoundError))
      case _                     => Left(ErrorWrapper(responseWrapper.correlationId, InternalError))
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

    val extraHipErrors = Map(
      "001" -> InternalError,
      "006" -> NotFoundError,
      "007" -> InternalError,
      "008" -> NoBusinessFoundError
    )

    errors ++ extraIfsErrors ++ extraHipErrors
  }

}
