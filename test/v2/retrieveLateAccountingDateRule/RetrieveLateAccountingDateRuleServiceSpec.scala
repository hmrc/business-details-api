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

package v2.retrieveLateAccountingDateRule

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v2.retrieveLateAccountingDateRule.model.request.RetrieveLateAccountingDateRuleRequest
import v2.retrieveLateAccountingDateRule.model.response.RetrieveLateAccountingDateRuleResponse

import scala.concurrent.Future

class RetrieveLateAccountingDateRuleServiceSpec extends ServiceSpec {

  private val nino        = Nino("AA123456A")
  private val businessId  = BusinessId("XAIS12345678910")
  private val taxYear     = TaxYear.fromMtd("2024-25")
  private val requestData = RetrieveLateAccountingDateRuleRequest(nino, businessId, taxYear)

  private val expectedResponse =
    RetrieveLateAccountingDateRuleResponse(
      disapply = true,
      eligible = true,
      Some(TaxYear.fromMtd("2024-25")),
      Some(TaxYear.fromMtd("2024-25"))
    )

  "service" when {
    "a connector call is successful" should {
      "return a mapped result" in new Test {
        MockRetrieveLateAccountingDateRuleConnector
          .retrieveLateAccountingDateRule(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, expectedResponse))))

        val result: ServiceOutcome[RetrieveLateAccountingDateRuleResponse] = await(service.retrieveLateAccountingDateRule(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, expectedResponse))
      }
    }
    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test {
          MockRetrieveLateAccountingDateRuleConnector
            .retrieveLateAccountingDateRule(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[RetrieveLateAccountingDateRuleResponse] = await(service.retrieveLateAccountingDateRule(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("1215", NinoFormatError),
        ("1117", TaxYearFormatError),
        ("1007", BusinessIdFormatError),
        ("1122", InternalError),
        ("1229", InternalError),
        ("5009", InternalError),
        ("5010", NotFoundError),
        ("UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError)
      )

      errors.foreach((serviceError _).tupled)
    }
  }

  private trait Test extends MockRetrieveLateAccountingDateRuleConnector {

    protected val service = new RetrieveLateAccountingDateRuleService(mockRetrieveLateAccountingDateRuleConnector)

  }

}
