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
import config.{ConfidenceLevelConfig, MockAppConfig}
import org.scalamock.handlers.CallHandler
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class EnrolmentsAuthServiceSpec extends ServiceSpec with MockAppConfig {

  "calling .authorised" when {
    val inputPredicate = EmptyPredicate

    "confidence level checks are on" should {
      behave like authService(authValidationEnabled = true, extraPredicatesAnd(inputPredicate))
    }

    "confidence level checks are off" should {
      behave like authService(authValidationEnabled = false, inputPredicate)
    }

    "Secondary Agent has permissions to access" should {
      behave like authorisedSecondaryAgents(inputPredicate, true, extraPredicatesAnd(inputPredicate), true)
    }

    "Secondary Agent does not have permissions to access" should {
      behave like authorisedSecondaryAgents(inputPredicate, true, extraPredicatesAnd(inputPredicate), false)
    }

    def authService(authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit = {
      behave like authorisedIndividual(inputPredicate, authValidationEnabled, expectedPredicate)
      behave like authorisedOrganisation(inputPredicate, authValidationEnabled, expectedPredicate)

      behave like authorisedAgentsMissingArn(inputPredicate, authValidationEnabled, expectedPredicate)
      behave like authorisedAgents(inputPredicate, authValidationEnabled, expectedPredicate)

      behave like disallowUsersWithoutEnrolments(inputPredicate, authValidationEnabled, expectedPredicate)
      behave like disallowWhenNotLoggedIn(inputPredicate, authValidationEnabled, expectedPredicate)
    }

    def authorisedIndividual(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit =
      "allow authorised individuals" in new Test {
        val retrievalsResult = new ~(Some(Individual), Enrolments(Set.empty))
        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
          .returns(Future.successful(retrievalsResult))

        await(enrolmentsAuthService.authorised(inputPredicate)) shouldBe Right(UserDetails("", "Individual", None))
      }

    def authorisedOrganisation(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit =
      "allow authorised organisations" in new Test {
        val retrievalsResult = new ~(Some(Organisation), Enrolments(Set.empty))
        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
          .returns(Future.successful(retrievalsResult))

        await(enrolmentsAuthService.authorised(inputPredicate)) shouldBe Right(UserDetails("", "Organisation", None))
      }

    def authorisedAgentsMissingArn(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit = {
      "disallow agents that are missing an ARN" in new Test {
        //        val enrolmentsWithoutArn: Enrolments = Enrolments(
        //          Set(
        //            Enrolment(
        //              "HMRC-AS-AGENT",
        //              Seq(EnrolmentIdentifier("SomeOtherIdentifier", "123567890")),
        //              "Active"
        //            )
        //          )
        //        )
        val retrievalsResult = new ~(Some(Agent), Enrolments(Set.empty))

        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
          .returns(Future.successful(retrievalsResult))

        //        MockedAuthConnector
        //          .authorised(Agent and Enrolment("HMRC-AS-AGENT"), authorisedEnrolments)
        //          .returns(Future.successful(enrolmentsWithoutArn))

        await(enrolmentsAuthService.authorised(inputPredicate)) shouldBe Left(InternalError)
      }
    }

    def authorisedAgents(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit =
      "allow authorised agents with ARN" in new Test {
        val mtdId = "123567890"
        val arn   = "123567890"
        val enrolments: Enrolments = Enrolments(
          Set(
            Enrolment(
              "HMRC-MTD-IT",
              Seq(EnrolmentIdentifier("MTDITID", mtdId)),
              "Active",
              Some("mtd-it-auth")
            ),
            Enrolment(
              "HMRC-AS-AGENT",
              Seq(EnrolmentIdentifier("AgentReferenceNumber", arn)),
              "Active"
            )
          )
        )
        val retrievalsResult = new ~(Some(Agent), enrolments)

        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
          .returns(Future.successful(retrievalsResult))

        await(enrolmentsAuthService.authorised(inputPredicate)) shouldBe Right(UserDetails("", "Agent", Some(arn)))

      }

    def authorisedSecondaryAgents(inputPredicate: Predicate,
                                  authValidationEnabled: Boolean,
                                  expectedPredicate: Predicate,
                                  secondaryAgentAccessAllowed: Boolean): Unit =
      "allow Secondary Agents authorisation" in new Test {
        val mtdId = "123567890"
        val arn   = "123567890"
        val enrolments: Enrolments = Enrolments(
          Set(
            Enrolment(
              "HMRC-MTD-IT-SECONDARY",
              Seq(EnrolmentIdentifier("MTDITID", mtdId)),
              "Active",
              Some("mtd-it-auth-secondary")
            ),
            Enrolment(
              "HMRC-AS-AGENT",
              Seq(EnrolmentIdentifier("AgentReferenceNumber", arn)),
              "Active"
            )
          )
        )

        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        if (secondaryAgentAccessAllowed) {
          val retrievalsResult = new ~(Some(Agent), enrolments)
          MockedAuthConnector
            .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
            .returns(Future.successful(retrievalsResult))

          await(enrolmentsAuthService.authorised(inputPredicate, secondaryAgentAccessAllowed)) shouldBe Right(UserDetails("", "Agent", Some(arn)))
        } else {
          MockedAuthConnector
            .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
            .returns(Future.failed(FailedRelationship()))

          val result: AuthOutcome = await(enrolmentsAuthService.authorised(inputPredicate, secondaryAgentAccessAllowed))
          result shouldBe Left(ClientOrAgentNotAuthorisedError)
        }
      }

    def disallowWhenNotLoggedIn(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit =
      "disallow users that are not logged in" in new Test {
        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
          .returns(Future.failed(MissingBearerToken()))

        val result: AuthOutcome = await(enrolmentsAuthService.authorised(inputPredicate))
        result shouldBe Left(ClientOrAgentNotAuthorisedError)
      }

    def disallowUsersWithoutEnrolments(inputPredicate: Predicate, authValidationEnabled: Boolean, expectedPredicate: Predicate): Unit =
      "disallow users without enrolments" in new Test {
        mockConfidenceLevelCheckConfig(authValidationEnabled = authValidationEnabled)

        MockedAuthConnector
          .authorised(expectedPredicate, affinityGroup and authorisedEnrolments)
          .returns(Future.failed(InsufficientEnrolments()))

        await(enrolmentsAuthService.authorised(inputPredicate)) shouldBe Left(ClientOrAgentNotAuthorisedError)
      }
  }

  private def extraPredicatesAnd(predicate: Predicate): Predicate = predicate and
    ((Individual and ConfidenceLevel.L200) or Organisation or (Agent and Enrolment("HMRC-AS-AGENT")))

  trait Test {
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    lazy val enrolmentsAuthService       = new EnrolmentsAuthService(mockAuthConnector, mockAppConfig)

    object MockedAuthConnector {

      def authorised[A](predicate: Predicate, retrievals: Retrieval[A]): CallHandler[Future[A]] = {
        (mockAuthConnector
          .authorise[A](_: Predicate, _: Retrieval[A])(_: HeaderCarrier, _: ExecutionContext))
          .expects(predicate, retrievals, *, *)
      }

    }

    def mockConfidenceLevelCheckConfig(authValidationEnabled: Boolean): Unit = {
      MockedAppConfig.confidenceLevelConfig.returns(
        ConfidenceLevelConfig(
          confidenceLevel = ConfidenceLevel.L200,
          definitionEnabled = true,
          authValidationEnabled = authValidationEnabled
        )
      )
    }

  }

}
