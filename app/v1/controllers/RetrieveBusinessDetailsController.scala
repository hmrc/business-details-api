/*
 * Copyright 2022 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.RetrieveBusinessDetailsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRawData
import v1.models.response.retrieveBusinessDetails.RetrieveBusinessDetailsHateoasData
import v1.services.{EnrolmentsAuthService, MtdIdLookupService, RetrieveBusinessDetailsService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBusinessDetailsController @Inject()(val authService: EnrolmentsAuthService,
                                                  val lookupService: MtdIdLookupService,
                                                  val idGenerator: IdGenerator,
                                                  parser: RetrieveBusinessDetailsRequestParser,
                                                  service: RetrieveBusinessDetailsService,
                                                  hateoasFactory: HateoasFactory,
                                                  cc: ControllerComponents)(implicit ec: ExecutionContext)
extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveBusinessDetailsController", endpointName = "Retrieve Business Details")

  def handleRequest(nino: String, businessId: String): Action[AnyContent] =
    authorisedAction(nino).async {implicit request =>

      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")

      val rawData = RetrieveBusinessDetailsRawData(nino, businessId)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](parser.parseRequest(rawData))
          serviceResponse <- EitherT(service.retrieveBusinessDetailsService(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, RetrieveBusinessDetailsHateoasData(nino, businessId)).asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error) match {
      case NinoFormatError | BadRequestError | BusinessIdFormatError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError | NoBusinessFoundError                      => NotFound(Json.toJson(errorWrapper))
      case DownstreamError                                           => InternalServerError(Json.toJson(errorWrapper))
      case _                                                         => unhandledError(errorWrapper)
    }
  }

}
