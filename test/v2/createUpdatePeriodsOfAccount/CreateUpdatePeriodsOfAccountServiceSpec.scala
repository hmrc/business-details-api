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

package v2.createUpdatePeriodsOfAccount

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequest
import v2.fixtures.CreateUpdatePeriodsOfAccountFixtures.minimumRequestBodyModel

import scala.concurrent.Future

class CreateUpdatePeriodsOfAccountServiceSpec extends ServiceSpec {

  private val nino: Nino             = Nino("AA123456A")
  private val businessId: BusinessId = BusinessId("XAIS12345678910")
  private val taxYear: TaxYear       = TaxYear.fromMtd("2025-26")

  private val request: CreateUpdatePeriodsOfAccountRequest = CreateUpdatePeriodsOfAccountRequest(
    nino = nino,
    businessId = businessId,
    taxYear = taxYear,
    body = minimumRequestBodyModel
  )

  "CreateUpdatePeriodsOfAccountService" when {
    "the connector call is successful" should {
      "return a mapped result" in new Test {
        MockedCreateUpdatePeriodsOfAccountConnector
          .createUpdate(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: ServiceOutcome[Unit] = await(service.createUpdate(request))
        result shouldBe Right(ResponseWrapper(correlationId, ()))

      }
    }

    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test {
          MockedCreateUpdatePeriodsOfAccountConnector
            .createUpdate(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[Unit] = await(service.createUpdate(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        "1000"                 -> InternalError,
        "1007"                 -> BusinessIdFormatError,
        "1117"                 -> TaxYearFormatError,
        "1128"                 -> RuleEndBeforeStartDateError,
        "1129"                 -> RuleStartDateError,
        "1130"                 -> RuleEndDateError,
        "1131"                 -> RulePeriodsOverlapError,
        "1132"                 -> RuleCessationDateError,
        "1215"                 -> NinoFormatError,
        "1216"                 -> InternalError,
        "4200"                 -> RuleOutsideAmendmentWindowError,
        "5000"                 -> RuleTaxYearNotSupportedError,
        "5010"                 -> NotFoundError,
        "UNMATCHED_STUB_ERROR" -> RuleIncorrectGovTestScenarioError
      )

      errors.foreach((serviceError _).tupled)
    }
  }

  private trait Test extends MockCreateUpdatePeriodsOfAccountConnector {

    protected val service: CreateUpdatePeriodsOfAccountService = new CreateUpdatePeriodsOfAccountService(
      connector = mockCreateUpdatePeriodsOfAccountConnector
    )

  }

}
