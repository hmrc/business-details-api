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

package v2.disapplyLateAccountingDateRule

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.MockHateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import api.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.MockAppConfig
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.json.JsValue
import play.api.mvc.Result
import routing.Version2
import utils.MockIdGenerator
import v2.disapplyLateAccountingDateRule.model.request.DisapplyLateAccountingDateRuleRequest

import scala.concurrent.Future

class DisapplyLateAccountingDateRuleControllerSpec
    extends ControllerBaseSpec(Version2)
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDisapplyLateAccountingDateRuleService
    with MockHateoasFactory
    with MockDisapplyLateAccountingDateRuleValidatorFactory
    with MockIdGenerator
    with MockAppConfig {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  private val validBusinessId                        = "XAIS12345678910"
  private val validTaxYear                           = "2024-25"
  private val parsedNino                             = Nino(nino)
  private val parsedBusinessId                       = BusinessId(validBusinessId)
  private val parsedTaxYear                          = TaxYear.fromMtd(validTaxYear)
  val userType: String                               = "Individual"
  val userDetails: UserDetails                       = UserDetails("mtdId", userType, None)

  private val requestData =
    DisapplyLateAccountingDateRuleRequest(parsedNino, parsedBusinessId, parsedTaxYear)

  "handleRequest" should {
    "return successful response with status OK" when {
      "valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDisapplyLateAccountingDateRuleService
          .disapply(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDisapplyLateAccountingDateRuleService
          .disapply(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTestWithAudit(TaxYearFormatError)
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller: DisapplyLateAccountingDateRuleController = new DisapplyLateAccountingDateRuleController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockDisapplyLateAccountingDateRuleService,
      auditService = mockAuditService,
      validatorFactory = mockDisapplyLateAccountingDateRuleValidatorFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.accountingTypeMinimumTaxYear
      .returns(2025)
      .anyNumberOfTimes()

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DisapplyLateAccountingDateRule",
        transactionName = "disapply-late-accounting-date-rule",
        detail = GenericAuditDetail(
          userType = "Individual",
          versionNumber = apiVersion.name,
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "businessId" -> validBusinessId, "taxYear" -> parsedTaxYear.asMtd),
          `X-CorrelationId` = correlationId,
          requestBody = None,
          auditResponse = auditResponse
        )
      )

    protected def callController(): Future[Result] = controller.handleRequest(nino, validBusinessId, validTaxYear)(
      fakeRequest.withHeaders(
        HeaderNames.AUTHORIZATION -> "Bearer Token"
      ))

  }

}
