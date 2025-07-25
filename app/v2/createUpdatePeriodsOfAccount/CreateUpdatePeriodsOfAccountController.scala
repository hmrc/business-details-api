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

import api.controllers._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import routing.Version
import utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateUpdatePeriodsOfAccountController @Inject() (
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    service: CreateUpdatePeriodsOfAccountService,
    auditService: AuditService,
    validatorFactory: CreateUpdatePeriodsOfAccountValidatorFactory,
    cc: ControllerComponents,
    val idGenerator: IdGenerator
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends AuthorisedController(cc) {

  val endpointName: String = "create-or-update-periods-of-account"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateUpdatePeriodsOfAccountController", endpointName = endpointName)

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear, request.body)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.createUpdate)
        .withAuditing(AuditHandler(
          auditService = auditService,
          auditType = "CreateOrUpdatePeriodsOfAccount",
          transactionName = "create-or-update-periods-of-account",
          apiVersion = Version(request),
          params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear),
          requestBody = Some(request.body)
        ))

      requestHandler.handleRequest()
    }

}
