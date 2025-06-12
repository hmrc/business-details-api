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

import api.controllers.ControllerTestRunner.validNino
import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import routing.Version2
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequest
import v2.fixtures.CreateUpdatePeriodsOfAccountFixtures.{fullRequestBodyModel, validFullRequestBodyJson}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateUpdatePeriodsOfAccountControllerSpec
    extends ControllerBaseSpec(version = Version2)
    with ControllerTestRunner
    with MockCreateUpdatePeriodsOfAccountService
    with MockCreateUpdatePeriodsOfAccountValidatorFactory {

  private val taxYear: String    = "2025-26"
  private val businessId: String = "X0IS12345678901"

  private val request: CreateUpdatePeriodsOfAccountRequest = CreateUpdatePeriodsOfAccountRequest(
    nino = Nino(validNino),
    businessId = BusinessId(businessId),
    taxYear = TaxYear.fromMtd(taxYear),
    body = fullRequestBodyModel
  )

  "CreateUpdatePeriodsOfAccountController" should {
    "return a successful response with status 204 (NO_CONTENT)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(request))

        MockCreateUpdatePeriodsOfAccountService
          .createUpdate(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT, maybeAuditRequestBody = Some(validFullRequestBodyJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError, Some(validFullRequestBodyJson))
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(request))

        MockCreateUpdatePeriodsOfAccountService
          .createUpdate(request)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, BusinessIdFormatError))))

        runErrorTestWithAudit(BusinessIdFormatError, Some(validFullRequestBodyJson))
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: CreateUpdatePeriodsOfAccountController = new CreateUpdatePeriodsOfAccountController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateUpdatePeriodsOfAccountValidatorFactory,
      service = mockCreateUpdatePeriodsOfAccountService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns true

    protected def callController(): Future[Result] =
      controller.handleRequest(validNino, businessId, taxYear)(fakePutRequest(validFullRequestBodyJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateOrUpdatePeriodsOfAccount",
        transactionName = "create-or-update-periods-of-account",
        detail = GenericAuditDetail(
          userType = "Individual",
          versionNumber = apiVersion.name,
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "businessId" -> businessId, "taxYear" -> taxYear),
          `X-CorrelationId` = correlationId,
          requestBody = Some(validFullRequestBodyJson),
          auditResponse = auditResponse
        )
      )

  }

}
