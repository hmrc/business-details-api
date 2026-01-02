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

package v2.listAllBusinesses

import api.controllers.*
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ListAllBusinessesController @Inject() (
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    service: ListAllBusinessesService,
    validatorFactory: ListAllBusinessDetailsValidatorFactory,
    cc: ControllerComponents,
    idGenerator: IdGenerator
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends AuthorisedController(cc) {

  val endpointName = "list-all-businesses"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "ListAllBusinessesController", endpointName = "List All Businesses")

  def handleRequest(nino: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.listAllBusinessesService)
          .withPlainJsonResult()
      requestHandler.handleRequest()
    }

}
