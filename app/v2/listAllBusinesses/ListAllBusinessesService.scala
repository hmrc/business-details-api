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

package v2.listAllBusinesses

import api.controllers.RequestContext
import api.models.errors.{InternalError, MtdError, NinoFormatError, NotFoundError, RuleIncorrectGovTestScenarioError}
import api.models.outcomes.ResponseWrapper
import api.services.{BaseService, ServiceOutcome}
import cats.data.EitherT
import cats.implicits.*
import v2.listAllBusinesses.model.request.ListAllBusinessesRequestData
import v2.listAllBusinesses.model.response.{Business, ListAllBusinessesResponse}
import v2.retrieveBusinessDetails.RetrieveBusinessDetailsConnector
import v2.retrieveBusinessDetails.model.response.downstream.RetrieveBusinessDetailsDownstreamResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListAllBusinessesService @Inject() (connector: RetrieveBusinessDetailsConnector) extends BaseService {

  def listAllBusinessesService(request: ListAllBusinessesRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext
  ): Future[ServiceOutcome[ListAllBusinessesResponse[Business]]] = {

    val result = for {
      downstreamResult <- EitherT(connector.retrieveBusinessDetails(request.nino).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap))))
    } yield toMtd(downstreamResult)

    result.value
  }

  private def toMtd(downstreamResult: ResponseWrapper[RetrieveBusinessDetailsDownstreamResponse]) =
    downstreamResult.map(ListAllBusinessesResponse.fromDownstream)

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

    val hipErrors = Map(
      "001" -> InternalError,
      "006" -> NotFoundError,
      "007" -> InternalError,
      "008" -> InternalError
    )

    errors ++ extraIfsErrors ++ hipErrors
  }

}
