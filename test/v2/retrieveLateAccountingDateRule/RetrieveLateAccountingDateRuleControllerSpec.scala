/*
 * Copyright 2026 HM Revenue & Customs
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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import play.api.mvc.Result
import routing.Version2
import v2.retrieveLateAccountingDateRule.fixture.RetrieveLateAccountingDateFixture.{mtdResponseJson, responseModel}
import v2.retrieveLateAccountingDateRule.model.request.RetrieveLateAccountingDateRuleRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveLateAccountingDateRuleControllerSpec
    extends ControllerBaseSpec(Version2)
    with ControllerTestRunner
    with MockRetrieveLateAccountingDateRuleValidatorFactory
    with MockRetrieveLateAccountingDateRuleService {

  private val businessId: String = "XAIS12345678910"
  private val taxYear: String    = "2025-26"

  private val requestData: RetrieveLateAccountingDateRuleRequest = RetrieveLateAccountingDateRuleRequest(
    Nino(nino),
    BusinessId(businessId),
    TaxYear.fromMtd(taxYear)
  )

  "RetrieveLateAccountingDateRuleController" should {
    "return 200 (OK) status" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveLateAccountingDateRuleService
          .retrieveLateAccountingDateRule(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveLateAccountingDateRuleService
          .retrieveLateAccountingDateRule(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }
  }

  private trait Test extends ControllerTest {

    protected val controller: RetrieveLateAccountingDateRuleController = new RetrieveLateAccountingDateRuleController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveLateAccountingDateRuleValidatorFactory,
      service = mockRetrieveLateAccountingDateRuleService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches
      .anyNumberOfTimes()
      .returns(
        Configuration("supporting-agents-access-control.enabled" -> true)
      )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes().returns(true)

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeGetRequest)

  }

}
