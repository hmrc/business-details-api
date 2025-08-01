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

package v1.createAmendQuarterlyPeriodType

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v1.createAmendQuarterlyPeriodType.def1.model.request._

import scala.concurrent.Future

class CreateAmendQuarterlyPeriodTypeServiceSpec extends ServiceSpec {

  private val nino        = Nino("AA123456A")
  private val businessId  = BusinessId("XAIS12345678910")
  private val taxYear     = TaxYear.fromMtd("2023-24")
  private val body        = Def1_CreateAmendQuarterlyPeriodTypeRequestBody(QuarterlyPeriodType.`standard`)
  private val requestData = Def1_CreateAmendQuarterlyPeriodTypeRequestData(nino, businessId, taxYear, body)

  "service" when {
    "a connector call is successful" should {
      "return a mapped result" in new Test {
        MockedCreateAmendQuarterlyPeriodTypeConnector
          .create(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: ServiceOutcome[Unit] = await(service.create(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test {
          MockedCreateAmendQuarterlyPeriodTypeConnector
            .create(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[Unit] = await(service.create(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        "INVALID_TAX_YEAR"                 -> TaxYearFormatError,
        "INVALID_CORRELATION_ID"           -> InternalError,
        "INVALID_TAXABLE_ENTITY_ID"        -> NinoFormatError,
        "INVALID_INCOME_SOURCE_ID"         -> BusinessIdFormatError,
        "INVALID_PAYLOAD"                  -> RuleIncorrectOrEmptyBodyError,
        "INCOME_SOURCE_NOT_FOUND"          -> RuleBusinessIdNotFoundError,
        "INCOME_SOURCE_STATE_CONFLICT"     -> RuleBusinessIdStateConflictError,
        "INVALID_PATH_PARAMETERS"          -> InternalError,
        "WRONG_TAX_YEAR_PROVIDED"          -> InternalError,
        "REQUIRED_PARAMETER_MISSING"       -> InternalError,
        "INVALID_INCOME_SOURCE_TYPE"       -> InternalError,
        "INVALID_REQUEST_SUBMISSION"       -> RuleBusinessIdStateConflictError,
        "ANNUAL_INCOME_SOURCE"             -> RuleBusinessIdStateConflictError,
        "QUARTER_REPORTING_UPDATING_ERROR" -> RuleQuarterlyPeriodUpdatingError,
        "SERVER_ERROR"                     -> InternalError,
        "SERVICE_UNAVAILABLE"              -> InternalError,
        "1000"                             -> RuleIncorrectOrEmptyBodyError,
        "1007"                             -> BusinessIdFormatError,
        "1117"                             -> TaxYearFormatError,
        "1121"                             -> RuleBusinessIdStateConflictError,
        "1122"                             -> InternalError,
        "1123"                             -> RuleBusinessIdStateConflictError,
        "1124"                             -> RuleBusinessIdStateConflictError,
        "1125"                             -> RuleQuarterlyPeriodUpdatingError,
        "1215"                             -> NinoFormatError,
        "1216"                             -> InternalError,
        "5010"                             -> RuleBusinessIdNotFoundError
      )

      errors.foreach((serviceError _).tupled)
    }
  }

  private trait Test extends MockCreateAmendQuarterlyPeriodTypeConnector {

    protected val service = new CreateAmendQuarterlyPeriodTypeService(mockCreateAmendQuarterlyPeriodTypeConnector)

  }

}
