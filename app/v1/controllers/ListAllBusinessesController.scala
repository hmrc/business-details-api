/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.ListAllBusinessesRequestParser
import v1.models.errors.{BadRequestError, DownstreamError, ErrorWrapper, NinoFormatError, NotFoundError}
import v1.models.request.listAllBusinesses.ListAllBusinessesRawData
import v1.services.{AuditService, EnrolmentsAuthService, ListAllBusinessesService, MtdIdLookupService}
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditEvent, AuditResponse, ListAllBusinessesAuditDetail}
import v1.models.response.listAllBusiness.ListAllBusinessesHateoasData

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListAllBusinessesController @Inject()(val authService: EnrolmentsAuthService,
                                            val lookupService: MtdIdLookupService,
                                            val idGenerator: IdGenerator,                                            requestDataParser: ListAllBusinessesRequestParser,
                                            service: ListAllBusinessesService,
                                            hateoasFactory: HateoasFactory,
                                            auditService: AuditService,
                                            cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "ListAllBusinessesController", endpointName = "List All Businesses")

  def handleRequest(nino: String): Action[AnyContent] =
    authorisedAction(nino).async {implicit request =>

      implicit val correlationId: String = idGenerator.getCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"with correlationId : $correlationId")

      val rawData = ListAllBusinessesRawData(nino)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestDataParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.listAllBusinessesService(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrapList(serviceResponse.responseData, ListAllBusinessesHateoasData(nino))
              .asRight[ErrorWrapper]
          )
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          val response = Json.toJson(vendorResponse)

          auditSubmission(ListAllBusinessesAuditDetail(
            userDetails = request.userDetails,
            nino = nino,
            `X-CorrelationId` = serviceResponse.correlationId,
            AuditResponse(OK, Right(Some(response)))))

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(ListAllBusinessesAuditDetail(request.userDetails, nino,
          resCorrelationId, AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case NinoFormatError | BadRequestError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError                     => NotFound(Json.toJson(errorWrapper))
      case DownstreamError                   => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: ListAllBusinessesAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("ListAllBusinesses", "list-all-businesses", details)
    auditService.auditEvent(event)
  }

}
