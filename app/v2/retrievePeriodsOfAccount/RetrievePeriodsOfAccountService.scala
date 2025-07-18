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

package v2.retrievePeriodsOfAccount

import api.controllers.RequestContext
import api.models.errors._
import api.services.{BaseService, ServiceOutcome}
import cats.implicits._
import v2.retrievePeriodsOfAccount.model.request.RetrievePeriodsOfAccountRequest
import v2.retrievePeriodsOfAccount.model.response.RetrievePeriodsOfAccountResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrievePeriodsOfAccountService @Inject() (connector: RetrievePeriodsOfAccountConnector) extends BaseService {

  def retrievePeriodsOfAccount(request: RetrievePeriodsOfAccountRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrievePeriodsOfAccountResponse]] = {

    connector.retrievePeriodsOfAccount(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))
  }

  private val downstreamErrorMap: Map[String, MtdError] = {
    Map(
      "1215"                 -> NinoFormatError,
      "1117"                 -> TaxYearFormatError,
      "1007"                 -> BusinessIdFormatError,
      "1216"                 -> InternalError,
      "5009"                 -> InternalError,
      "5010"                 -> NotFoundError,
      "UNMATCHED_STUB_ERROR" -> RuleIncorrectGovTestScenarioError
    )
  }

}
