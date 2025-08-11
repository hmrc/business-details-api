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

package v2.retrieveAccountingType

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.auth.UserDetails
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import api.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Result
import routing.Version2
import utils.MockIdGenerator
import v2.common.models.AccountingType.CASH
import v2.retrieveAccountingType.model.request.RetrieveAccountingTypeRequest
import v2.retrieveAccountingType.model.response.RetrieveAccountingTypeResponse

import scala.concurrent.Future

class RetrieveAccountingTypeControllerSpec
    extends ControllerBaseSpec(Version2)
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveAccountingTypeValidatorFactory
    with MockRetrieveAccountingTypeService
    with MockIdGenerator {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  private val businessId                             = "XAIS12345678910"
  private val taxYear                                = "2024-25"
  val userType: String                               = "Individual"
  val userDetails: UserDetails                       = UserDetails("mtdId", userType, None)

  private val validBody = Json.parse("""
      |{
      | "accountingType": "CASH"
      |}
      |""".stripMargin)

  private val parsedNino       = Nino(nino)
  private val parsedBusinessId = BusinessId(businessId)
  private val parsedTaxYear    = TaxYear.fromMtd(taxYear)

  private val requestData =
    RetrieveAccountingTypeRequest(parsedNino, parsedBusinessId, parsedTaxYear)

  private val responseData = RetrieveAccountingTypeResponse(CASH)

  "handleRequest" should {
    "return successful response with status OK" when {
      "valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveAccountingTypeService
          .retrieveAccountingType(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))
        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(validBody))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveAccountingTypeService
          .retrieveAccountingType(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }
  }

  private trait Test extends ControllerTest {

    val controller = new RetrieveAccountingTypeController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveAccountingTypeValidatorFactory,
      service = mockRetrieveAccountingTypeService,
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
