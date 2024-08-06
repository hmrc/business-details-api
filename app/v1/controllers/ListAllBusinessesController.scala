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

package v1.controllers

import api.controllers._
import api.hateoas.HateoasFactory
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import config.{AppConfig, FeatureSwitches}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator
import v1.controllers.validators.ListAllBusinessDetailsValidatorFactory
import v1.models.response.listAllBusinesses.ListAllBusinessesHateoasData
import v1.services.ListAllBusinessesService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ListAllBusinessesController @Inject() (
    val authService: EnrolmentsAuthService,
    val lookupService: MtdIdLookupService,
    service: ListAllBusinessesService,
    validatorFactory: ListAllBusinessDetailsValidatorFactory,
    hateoasFactory: HateoasFactory,
    cc: ControllerComponents,
    idGenerator: IdGenerator
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends AuthorisedController(cc) {

  val endpointName = "list-all-businesses"

  lazy protected val supportingAgentsAccessControlEnabled: Boolean =
    FeatureSwitches(appConfig).supportingAgentsAccessControlEnabled

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
          .withResultCreator(ResultCreator.hateoasListWrapping(hateoasFactory)((_, _) => ListAllBusinessesHateoasData(nino)))

      requestHandler.handleRequest()
    }

}
