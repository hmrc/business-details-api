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

package v2.disapplyLateAccountingDateRule

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v2.disapplyLateAccountingDateRule.model.request.DisapplyLateAccountingDateRuleRequest

import scala.concurrent.Future

class DisapplyLateAccountingDateRuleServiceSpec extends ServiceSpec {

  private val nino        = Nino("AA123456A")
  private val businessId  = BusinessId("XAIS12345678910")
  private val taxYear     = TaxYear.fromMtd("2024-25")
  private val requestData = DisapplyLateAccountingDateRuleRequest(nino, businessId, taxYear)

  "DisapplyLateAccountingDateRuleService" when {
    "a connector call is successful" should {
      "return a mapped result" in new Test {
        MockDisapplyLateAccountingDateRuleConnector
          .disapply(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: ServiceOutcome[Unit] = await(service.disapply(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test {
          MockDisapplyLateAccountingDateRuleConnector
            .disapply(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[Unit] = await(service.disapply(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("1007", BusinessIdFormatError),
        ("1117", TaxYearFormatError),
        ("1215", NinoFormatError),
        ("1216", InternalError),
        ("5009", InternalError),
        ("5010", NotFoundError),
        ("1115", RuleTaxYearNotEndedError),
        ("1133", RuleNoAccountingDateFoundError),
        ("1134", RuleElectionPeriodNotExpiredError),
        ("4200", RuleOutsideAmendmentWindowError),
        ("1242", RuleTypeOfBusinessIncorrectError),
        ("5000", RuleTaxYearNotSupportedError)
      )

      errors.foreach((serviceError _).tupled)
    }
  }

  private trait Test extends MockDisapplyLateAccountingDateRuleConnector {
    protected val service = new DisapplyLateAccountingDateRuleService(mockDisapplyLateAccountingDateRuleConnector)
  }

}
