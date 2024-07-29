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

package api.services

import api.models.auth.UserDetails
import api.models.errors.{ClientOrAgentNotAuthorisedError, InternalError}
import api.models.outcomes.AuthOutcome
import config.AppConfig
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentsAuthService @Inject() (val connector: AuthConnector, val appConfig: AppConfig) extends Logging {

  private val authFunction: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = connector
  }

  def buildPredicate(predicate: Predicate): Predicate =
    if (appConfig.confidenceLevelConfig.authValidationEnabled) {
      predicate and ((Individual and ConfidenceLevel.L200) or Organisation or Agent)
    } else {
      predicate
    }

  def authorised(
      predicate: Predicate,
      endpointAllowsSecondaryAgents: Boolean = false
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuthOutcome] = {

    authFunction
      .authorised(buildPredicate(predicate))
      .retrieve(affinityGroup and authorisedEnrolments) {
        case Some(Individual) ~ _ =>
          Future.successful(Right(UserDetails("", "Individual", None)))

        case Some(Organisation) ~ _ =>
          Future.successful(Right(UserDetails("", "Organisation", None)))

        case Some(Agent) ~ authorisedEnrolments =>
          if (isSecondaryAgent(authorisedEnrolments) && !endpointAllowsSecondaryAgents) {
            Future.successful(Left(ClientOrAgentNotAuthorisedError))
          } else {
            retrieveAgentDetails(authorisedEnrolments) match {
              case arn @ Some(_) => Future.successful(Right(UserDetails("", "Agent", arn)))
              case None =>
                logger.warn(s"[EnrolmentsAuthService][authorised] No AgentReferenceNumber defined on agent enrolment.")
                Future.successful(Left(InternalError))
            }
          }

        case _ =>
          logger.warn(s"[EnrolmentsAuthService][authorised] Invalid AffinityGroup.")
          Future.successful(Left(ClientOrAgentNotAuthorisedError))
      }
      .recoverWith {
        case _: MissingBearerToken     => Future.successful(Left(ClientOrAgentNotAuthorisedError))
        case _: AuthorisationException => Future.successful(Left(ClientOrAgentNotAuthorisedError))
        case error =>
          logger.warn(s"[EnrolmentsAuthService][authorised] An unexpected error occurred: $error")
          Future.successful(Left(InternalError))
      }
  }

  private def isSecondaryAgent(authorisedEnrolments: Enrolments): Boolean = {
    authorisedEnrolments.getEnrolment("HMRC-MTD-IT-SECONDARY").isDefined
  }

  private def retrieveAgentDetails(authorisedEnrolments: Enrolments): Option[String] = {
    authorisedEnrolments
      .getEnrolment("HMRC-AS-AGENT")
      .flatMap(_.getIdentifier("AgentReferenceNumber"))
      .map(_.value)
  }

}
