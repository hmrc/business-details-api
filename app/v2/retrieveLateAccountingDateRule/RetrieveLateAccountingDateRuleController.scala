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

import api.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveLateAccountingDateRuleController @Inject() (
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    service: RetrieveLateAccountingDateRuleService,
    validatorFactory: RetrieveLateAccountingDateRuleValidatorFactory,
    cc: ControllerComponents,
    idGenerator: IdGenerator
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends AuthorisedController(cc) {
  val endpointName = "retrieve-late-accounting-date-rule"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveLateAccountingDateRule", endpointName)

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.retrieveLateAccountingDateRule)
          .withPlainJsonResult(OK)

      requestHandler.handleRequest()
    }

}
