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

package v2.updateAccountingType

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.MockHateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import routing.Version2
import utils.MockIdGenerator
import v2.common.models.AccountingType
import v2.updateAccountingType.model.request._

import scala.concurrent.Future

class UpdateAccountingTypeControllerSpec
    extends ControllerBaseSpec(Version2)
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockUpdateAccountingTypeService
    with MockHateoasFactory
    with MockUpdateAccountingTypeValidatorFactory
    with MockIdGenerator
    with MockAppConfig {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  private val versionNumber                          = "2.0"
  private val validBusinessId                        = "XAIS12345678910"
  private val validTaxYear                           = "2024-25"
  val userType: String                               = "Individual"
  val userDetails: UserDetails                       = UserDetails("mtdId", userType, None)

  private val validBody = Json.parse("""
      |{
      | "accountingType": "CASH"
      |}
      |""".stripMargin)

  private val parsedNino       = Nino(nino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)
  private val parsedBody       = UpdateAccountingTypeRequestBody(AccountingType.CASH)

  private val requestData =
    UpdateAccountingTypeRequestData(parsedNino, parsedBusinessId, parsedTaxYear, parsedBody)

  "handleRequest" should {
    "return successful response with status OK" when {
      "valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockUpdateAccountingTypeService
          .create(requestData)
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

        MockUpdateAccountingTypeService
          .create(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTestWithAudit(TaxYearFormatError)
      }
    }
  }

  private trait Test extends ControllerTest with MockAppConfig with AuditEventChecking[FlattenedGenericAuditDetail] {

    val controller = new UpdateAccountingTypeController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockUpdateAccountingTypeValidatorFactory,
      service = mockUpdateAccountingTypeService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.updateAccountingTypeMinimumTaxYear
      .returns(2025)
      .anyNumberOfTimes()

    protected def callController(): Future[Result] = controller.handleRequest(nino, validBusinessId, validTaxYear)(fakePutRequest(validBody))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "UpdateAccountingType",
        transactionName = "update-accounting-type",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some(versionNumber),
          userDetails = userDetails,
          params = Map("nino" -> nino, "businessId" -> validBusinessId, "taxYear" -> parsedTaxYear.asMtd, "accountingType" -> "CASH"),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
