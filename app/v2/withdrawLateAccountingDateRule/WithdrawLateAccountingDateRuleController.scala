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

package v2.withdrawLateAccountingDateRule

import api.controllers._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.{AppConfig, FeatureSwitches}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import routing.Version
import utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class WithdrawLateAccountingDateRuleController @Inject() (
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    service: WithdrawLateAccountingDateRuleService,
    auditService: AuditService,
    validatorFactory: WithdrawLateAccountingDateRuleValidatorFactory,
    cc: ControllerComponents,
    val idGenerator: IdGenerator
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends AuthorisedController(cc) {

  override val endpointName: String = "withdraw-late-accounting-date-rule"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "WithdrawLateAccountingDateRuleController", endpointName = endpointName)

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear, temporalValidationEnabled = FeatureSwitches(appConfig).isTemporalValidationEnabled)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.withdraw)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "WithdrawLateAccountingDateRule",
          transactionName = endpointName,
          apiVersion = Version(request),
          params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear)
        ))
        .withNoContentResult()

      requestHandler.handleRequest()
    }

}
