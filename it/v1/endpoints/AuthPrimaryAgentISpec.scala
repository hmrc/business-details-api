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

package v1.endpoints

import api.models.domain.TaxYear
import api.models.errors.{ClientOrAgentNotAuthorisedError, InternalError}
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.{FORBIDDEN, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class AuthPrimaryAgentISpec extends IntegrationBaseSpec {

  private val primaryAgentOnlyEndpoint = "create-amend-quarterly-period-type"

  /** One endpoint where supporting agents are allowed.
    */
  override def servicesConfig: Map[String, String] =
    Map(
      s"api.supporting-agent-endpoints.$primaryAgentOnlyEndpoint" -> "false"
    ) ++ super.servicesConfig

  private val nino       = "AA123456A"
  private val businessId = "XAIS12345678901"
  private val taxYear    = TaxYear.fromMtd("2024-25")

  private val mtdUrl = s"/$nino/$businessId/${taxYear.asMtd}"

  private val requestJson = Json.parse("""
    |{
    | "quarterlyPeriodType": "standard"
    |}
    |""".stripMargin)

  private val downstreamRequestJson = Json.parse("""
    |{
    | "QRT": "Standard"
    |}
    |""".stripMargin)

  "Calling an endpoint that only allows primary agents" when {
    "the client is the primary agent" should {
      "return a success response" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)

          AuthStub.authorisedWithAgentAffinityGroup()
          AuthStub.authorisedWithPrimaryAgentEnrolment()

          DownstreamStub
            .when(DownstreamStub.PUT, downstreamUri)
            .withRequestBody(downstreamRequestJson)
            .thenReturn(OK, JsObject.empty)
        }

        val response: WSResponse = sendMtdRequest()
        response.status shouldBe NO_CONTENT
      }
    }

    "the client is a supporting agent" should {
      "return a 403 response" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)

          AuthStub.authorisedWithAgentAffinityGroup()
          AuthStub.unauthorisedForPrimaryAgentEnrolment()
        }

        val response: WSResponse = sendMtdRequest()

        response.status shouldBe FORBIDDEN
        response.body should include(ClientOrAgentNotAuthorisedError.message)
      }
    }
  }

  "Calling an endpoint" when {

    "MTD ID lookup succeeds but the user isn't logged in" should {

      "return a 403 response" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = sendMtdRequest()
        response.status shouldBe FORBIDDEN
      }
    }

    "an MTD ID is retrieved from the NINO but the user isn't authorised to access it" should {

      "return a 403 response" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = sendMtdRequest()
        response.status shouldBe FORBIDDEN
        response.body should include(ClientOrAgentNotAuthorisedError.message)
      }
    }

    "MTD ID lookup fails with a 500" should {

      "return a 500 response" in new Test {
        def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.error(nino, INTERNAL_SERVER_ERROR)
        }

        val response: WSResponse = sendMtdRequest()
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.body should include(InternalError.message)
      }
    }

  }

  private trait Test {

    protected def setupStubs(): StubMapping

    def sendMtdRequest(): WSResponse = await(request.put(requestJson))

    private def request: WSRequest = {
      AuthStub.resetAll()
      setupStubs()

      buildRequest(mtdUrl)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    protected def downstreamUri: String =
      s"/income-tax/${taxYear.asTysDownstream}/income-sources/reporting-type/$nino/$businessId"

  }

}
