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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain._
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Result
import routing.Version2
import utils.MockIdGenerator
import v2.common.models.PeriodsOfAccountDates
import v2.retrievePeriodsOfAccount.model.request.RetrievePeriodsOfAccountRequest
import v2.retrievePeriodsOfAccount.model.response.RetrievePeriodsOfAccountResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrievePeriodsOfAccountControllerSpec
    extends ControllerBaseSpec(Version2)
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrievePeriodsOfAccountService
    with MockRetrievePeriodsOfAccountValidatorFactory
    with MockIdGenerator
    with MockAppConfig {

  private val businessId = "XAIS12345678910"
  private val taxYear    = "2026"

  private val responseBody = Json.parse(
    """
      |{
      |  "periodsOfAccount": true,
      |  "periodsOfAccountDates": [
      |    {
      |      "startDate": "2025-04-06",
      |      "endDate": "2025-07-05"
      |    },
      |    {
      |      "startDate": "2025-07-06",
      |      "endDate": "2025-10-05"
      |    },
      |    {
      |      "startDate": "2025-10-06",
      |      "endDate": "2025-01-05"
      |    },
      |    {
      |      "startDate": "2025-01-06",
      |      "endDate": "2025-04-05"
      |    }
      |  ]
      |}
        """.stripMargin
  )

  private val responseData = RetrievePeriodsOfAccountResponse(
    true,
    Some(
      Seq(
        PeriodsOfAccountDates(
          "2025-04-06",
          "2025-07-05"
        ),
        PeriodsOfAccountDates(
          "2025-07-06",
          "2025-10-05"
        ),
        PeriodsOfAccountDates(
          "2025-10-06",
          "2025-01-05"
        ),
        PeriodsOfAccountDates(
          "2025-01-06",
          "2025-04-05"
        )
      )
    )
  )

  private val requestData = RetrievePeriodsOfAccountRequest(Nino(nino), BusinessId(businessId), TaxYear(taxYear))

  "handleRequest" should {
    "return successful response with status OK" when {
      "valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrievePeriodsOfAccountService
          .retrievePeriodsOfAccountService(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseBody))

      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrievePeriodsOfAccountService
          .retrievePeriodsOfAccountService(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrievePeriodsOfAccountController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrievePeriodsOfAccountValidatorFactory,
      service = mockRetrievePeriodsOfAccountService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeGetRequest)
  }

}
