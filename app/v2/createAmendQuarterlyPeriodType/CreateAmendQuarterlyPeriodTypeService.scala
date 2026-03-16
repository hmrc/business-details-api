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

package v2.createAmendQuarterlyPeriodType

import api.controllers.RequestContext
import api.models.errors.*
import api.services.{BaseService, ServiceOutcome}
import cats.implicits.*
import v2.createAmendQuarterlyPeriodType.model.request.CreateAmendQuarterlyPeriodTypeRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendQuarterlyPeriodTypeService @Inject() (connector: CreateAmendQuarterlyPeriodTypeConnector) extends BaseService {

  def create(request: CreateAmendQuarterlyPeriodTypeRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] =
    connector.create(request).map(_.leftMap(mapDownstreamErrors(downstreamErrorMap)))

  private val downstreamErrorMap: Map[String, MtdError] = {
    Map(
      "1000" -> RuleIncorrectOrEmptyBodyError,
      "1007" -> BusinessIdFormatError,
      "1117" -> TaxYearFormatError,
      "1121" -> RuleBusinessIdStateConflictError,
      "1122" -> RuleRequestCannotBeFulfilledError.incomeSourceTypeMsg,
      "1123" -> RuleBusinessIdStateConflictError,
      "1124" -> RuleBusinessIdStateConflictError,
      "1125" -> RuleQuarterlyPeriodUpdatingError,
      "1215" -> NinoFormatError,
      "1216" -> InternalError,
      "5010" -> RuleBusinessIdNotFoundError
    )
  }

}
