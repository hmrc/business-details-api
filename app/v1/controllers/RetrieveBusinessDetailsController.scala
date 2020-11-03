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
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.controllers.requestParsers.RetrieveBusinessDetailsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditEvent, AuditResponse, RetrieveBusinessDetailsAuditDetail}
import v1.models.errors._
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRawData
import v1.models.response.retrieveBusinessDetails.RetrieveBusinessDetailsHateoasData
import v1.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService, RetrieveBusinessDetailsService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBusinessDetailsController @Inject()(val authService: EnrolmentsAuthService,
                                                  val lookupService: MtdIdLookupService,
                                                  parser: RetrieveBusinessDetailsRequestParser,
                                                  service: RetrieveBusinessDetailsService,
                                                  hateoasFactory: HateoasFactory,
                                                  auditService: AuditService,
                                                  cc: ControllerComponents)(implicit ec: ExecutionContext)
extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveBusinessDetailsController", endpointName = "Retrieve Business Details")

  def handleRequest(nino: String, businessId: String): Action[AnyContent] =
    authorisedAction(nino).async {implicit request =>
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

          val response = Json.toJson(vendorResponse)

          auditSubmission(RetrieveBusinessDetailsAuditDetail(
            userDetails = request.userDetails,
            nino = nino,
            `X-CorrelationId` = serviceResponse.correlationId,
            AuditResponse(OK, Right(Some(response)))))

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)

        auditSubmission(RetrieveBusinessDetailsAuditDetail(request.userDetails, nino,
          correlationId, AuditResponse(result.header.status, Left(errorWrapper.auditErrors))))
        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case NinoFormatError | BadRequestError | BusinessIdFormatError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError | NoBusinessFoundError                      => NotFound(Json.toJson(errorWrapper))
      case DownstreamError                                           => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: RetrieveBusinessDetailsAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext) = {
    val event = AuditEvent("RetrieveBusinessDetails", "retrieve-business-details", details)
    auditService.auditEvent(event)
  }

}
