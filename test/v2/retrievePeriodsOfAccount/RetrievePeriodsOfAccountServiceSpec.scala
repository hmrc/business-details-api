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

package v2.retrievePeriodsOfAccount

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v2.common.models.PeriodsOfAccountDates
import v2.retrievePeriodsOfAccount.model.request._
import v2.retrievePeriodsOfAccount.model.response.RetrievePeriodsOfAccountResponse

import scala.concurrent.Future

class RetrievePeriodsOfAccountServiceSpec extends ServiceSpec {

  private val nino        = Nino("AA123456A")
  private val businessId  = BusinessId("XAIS12345678910")
  private val taxYear     = TaxYear.fromMtd("2024-25")
  private val requestData = RetrievePeriodsOfAccountRequest(nino, businessId, taxYear)

  private val expectedResponse = RetrievePeriodsOfAccountResponse(
    true,
    Some(
      Seq(
        PeriodsOfAccountDates("2025-04-06", "2025-07-05"),
        PeriodsOfAccountDates("2025-07-06", "2025-10-05"),
        PeriodsOfAccountDates("2025-10-06", "2025-01-05"),
        PeriodsOfAccountDates("2025-01-06", "2025-04-05")
      )
    )
  )

  "service" when {
    "a connector call is successful" should {
      "return a mapped result" in new Test {
        MockedRetrievePeriodsOfAccountConnector
          .retrievePeriodsOfAccount(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, expectedResponse))))

        val result: ServiceOutcome[RetrievePeriodsOfAccountResponse] = await(service.retrievePeriodsOfAccount(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, expectedResponse))
      }
    }
    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test {
          MockedRetrievePeriodsOfAccountConnector
            .retrievePeriodsOfAccount(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[RetrievePeriodsOfAccountResponse] = await(service.retrievePeriodsOfAccount(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("1215", NinoFormatError),
        ("1117", TaxYearFormatError),
        ("1007", BusinessIdFormatError),
        ("1216", InternalError),
        ("5009", InternalError),
        ("5010", NotFoundError),
        ("UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError)
      )

      errors.foreach((serviceError _).tupled)
    }
  }

  private trait Test extends MockRetrievePeriodsOfAccountConnector {

    protected val service = new RetrievePeriodsOfAccountService(mockRetrievePeriodsOfAccountConnector)

  }

}
