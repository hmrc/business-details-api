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
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.errors.ErrorWrapper
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import routing.{Version, Version1}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.IdGenerator
import v1.controllers.validators.CreateAmendQuarterlyPeriodTypeValidatorFactory
import v1.services.CreateAmendQuarterlyPeriodTypeService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendQuarterlyPeriodTypeController @Inject() (val authService: EnrolmentsAuthService,
                                                          val lookupService: MtdIdLookupService,
                                                          service: CreateAmendQuarterlyPeriodTypeService,
                                                          auditService: AuditService,
                                                          validatorFactory: CreateAmendQuarterlyPeriodTypeValidatorFactory,
                                                          cc: ControllerComponents,
                                                          val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateAmendQuarterlyPeriodTypeController", endpointName = "create-amend-quarterly-period-type")

  def handleRequest(nino: String, businessId: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, businessId, taxYear, request.body)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.create)
          .withAuditing(auditHandler(nino, businessId, taxYear, request))

      requestHandler.handleRequest()
    }


  private def auditHandler(nino: String,
                           businessId: String,
                           taxYear: String,
                           request: UserRequest[JsValue]): AuditHandler = {
    new AuditHandler() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]])(implicit
                                                                                                                            ctx: RequestContext,
                                                                                                                            ec: ExecutionContext): Unit = {
        val versionNumber = Version.from(request, orElse = Version1)
        val quarterlyPeriodType = (request.request.body \ "quarterlyPeriodType").asOpt[String] match {
          case Some(x) => Map("quarterlyPeriodType" -> x)
          case None => Map.empty[String, String]
        }
        val params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear) ++ quarterlyPeriodType

        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(
              FlattenedGenericAuditDetail(
                Some(versionNumber.name),
                request.userDetails,
                params,
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, response = Left(err.auditErrors))
              ))
          case Right(_) =>
            auditSubmission(
              FlattenedGenericAuditDetail(
                Some(versionNumber.name),
                request.userDetails,
                params,
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, response = Right(None))
              ))
        }
      }
    }
  }

  private def auditSubmission(details: FlattenedGenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("CreateAndAmendQuarterlyPeriodTypeForABusiness", "create-and-amend-quarterly-period", details)
    auditService.auditEvent(event)
  }

}
