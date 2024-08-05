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

package api.controllers

import api.models.auth.UserDetails
import api.models.errors.{BadRequestError, ClientOrAgentNotAuthorisedError, InternalError, InvalidBearerTokenError, NinoFormatError}
import api.services.{EnrolmentsAuthService, MockEnrolmentsAuthService, MockMtdIdLookupService, MtdIdLookupService}
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorisedControllerSpec extends ControllerBaseSpec with MockAppConfig {

  private val nino  = "AA123456A"
  private val mtdId = "X123567890"

  "Calling an action" when {

    "the user is authorised" should {
      "return a 200" in new Test {
        MockedMtdIdLookupService
          .lookup(nino)
          .returns(Future.successful(Right(mtdId)))

        MockedEnrolmentsAuthService.authoriseUser()

        val result: Future[Result] = controller.action(nino)(fakeGetRequest)
        status(result) shouldBe OK
      }
    }

    "the Primary Agent is authorised and secondary agents aren't allowed for this endpoint" should {
      "return a 200" in new Test {
        override def endpointAllowsSecondaryAgents: Boolean = false

        MockedMtdIdLookupService.lookup(nino) returns Future.successful(Right(mtdId))

        MockedEnrolmentsAuthService
          .authoriseAgent(mtdId)
          .returns(Future.successful(Right(UserDetails("", "Agent", Some("arn")))))

        val result: Future[Result] = controller.action(nino)(fakeGetRequest)
        status(result) shouldBe OK
      }
    }

    "the Secondary Agent is authorised" should {
      "return a 200" in new Test {
        MockedMtdIdLookupService.lookup(nino) returns Future.successful(Right(mtdId))

        MockedEnrolmentsAuthService
          .authoriseAgent(mtdId, secondaryAgentAccessAllowed = true)
          .returns(Future.successful(Right(UserDetails("", "Agent", Some("arn")))))

        val result: Future[Result] = controller.action(nino)(fakeGetRequest)
        status(result) shouldBe OK
      }
    }

    "the Secondary Agent is not authorised" should {
      "return a 403" in new Test {
        MockedMtdIdLookupService.lookup(nino) returns Future.successful(Right(mtdId))

        MockedEnrolmentsAuthService
          .authoriseAgent(mtdId, secondaryAgentAccessAllowed = true)
          .returns(Future.successful(Left(ClientOrAgentNotAuthorisedError)))

        val result: Future[Result] = controller.action(nino)(fakeGetRequest)
        status(result) shouldBe FORBIDDEN
      }
    }

    "the EnrolmentsAuthService returns an error" should {
      "return that error with its status code" in new Test {
        MockedMtdIdLookupService.lookup(nino) returns Future.successful(Right(mtdId))

        MockedEnrolmentsAuthService
          .authoriseAgent(mtdId, secondaryAgentAccessAllowed = true)
          .returns(Future.successful(Left(BadRequestError)))

        val result: Future[Result] = controller.action(nino)(fakeGetRequest)
        status(result) shouldBe BadRequestError.httpStatus
        contentAsJson(result) shouldBe BadRequestError.asJson
      }
    }

    "the MtdIdLookupService returns an error" should {
      "return that error with its status code" in new Test {
        MockedMtdIdLookupService.lookup(nino) returns Future.successful(Left(BadRequestError))

        val result: Future[Result] = controller.action(nino)(fakeGetRequest)
        status(result) shouldBe BadRequestError.httpStatus
        contentAsJson(result) shouldBe BadRequestError.asJson
      }
    }

    "the nino is invalid" should {
      "return a 400" in new Test {
        MockedMtdIdLookupService
          .lookup(nino)
          .returns(Future.successful(Left(NinoFormatError)))

        val result: Future[Result] = controller.action(nino)(fakeGetRequest)
        status(result) shouldBe BAD_REQUEST
      }
    }

    "the nino is valid but invalid bearer token" should {
      "return a 401" in new Test {
        MockedMtdIdLookupService
          .lookup(nino)
          .returns(Future.successful(Left(InvalidBearerTokenError)))

        val result: Future[Result] = controller.action(nino)(fakeGetRequest)
        status(result) shouldBe UNAUTHORIZED
      }
    }

  }

  "authorisation checks fail when retrieving the MTD ID" should {
    "return a 403" in new Test {
      MockedMtdIdLookupService
        .lookup(nino)
        .returns(Future.successful(Left(ClientOrAgentNotAuthorisedError)))

      val result: Future[Result] = controller.action(nino)(fakeGetRequest)
      status(result) shouldBe FORBIDDEN
    }
  }

  "an error occurs retrieving the MTD ID" should {
    "return a 500" in new Test {
      MockedMtdIdLookupService
        .lookup(nino)
        .returns(Future.successful(Left(InternalError)))

      val result: Future[Result] = controller.action(nino)(fakeGetRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  trait Test extends MockEnrolmentsAuthService with MockMtdIdLookupService {
    val hc: HeaderCarrier = HeaderCarrier()

    class TestController extends AuthorisedController(cc) {
      val endpointName = "test-endpoint"

      override val authService: EnrolmentsAuthService = mockEnrolmentsAuthService
      override val lookupService: MtdIdLookupService  = mockMtdIdLookupService

      def action(nino: String): Action[AnyContent] = authorisedAction(nino).async {
        Future.successful(Ok(JsObject.empty))
      }

    }

    lazy val controller = new TestController()

    protected def secondaryAgentsfeatureEnabled: Boolean = true

    protected def endpointAllowsSecondaryAgents: Boolean = true

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "secondary-agents-access-control.enabled" -> secondaryAgentsfeatureEnabled
    )

    MockedAppConfig
      .endpointAllowsSecondaryAgents(controller.endpointName)
      .anyNumberOfTimes() returns endpointAllowsSecondaryAgents

    protected final val primaryAgentPredicate: Predicate = Enrolment("HMRC-MTD-IT")
      .withIdentifier("MTDITID", mtdId)
      .withDelegatedAuthRule("mtd-it-auth")

    protected final val secondaryAgentPredicate: Predicate = Enrolment("HMRC-MTD-IT-SECONDARY")
      .withIdentifier("MTDITID", mtdId)
      .withDelegatedAuthRule("mtd-it-auth-secondary")

  }

}
