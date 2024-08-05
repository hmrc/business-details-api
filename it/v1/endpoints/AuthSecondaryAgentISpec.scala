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

import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class AuthSecondaryAgentISpec extends IntegrationBaseSpec {

  private val nino = "AA123456A"

  private def downstreamUri = s"/registration/business-details/nino/$nino"

  "Calling an endpoint that allows secondary agents" when {
    "the client is the primary agent" should {
      "return a success response" in new Test {
        def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)

          AuthStub.authorisedWithAgentAffinityGroup()
          AuthStub.authorisedWithPrimaryAgentEnrolment()

          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, downstreamResponse)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
      }
    }

    "the client is a secondary agent" should {
      "return a success response" in new Test {
        def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)

          AuthStub.authorisedWithAgentAffinityGroup()
          AuthStub.unauthorisedForPrimaryAgentEnrolment()
          AuthStub.authorisedWithSecondaryAgentEnrolment()

          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, downstreamResponse)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe OK
      }
    }
  }

  private trait Test {

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      AuthStub.resetAll()
      setupStubs()

      buildRequest(s"/$nino/list")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

  val downstreamResponse: JsValue = Json.parse(s"""
      |{
      |   "safeId": "XE00001234567890",
      |   "nino": "$nino",
      |   "mtdbsa": "123456789012345",
      |   "propertyIncome": false,
      |   "businessData": [
      |      {
      |         "incomeSourceType": "1",
      |         "incomeSourceId": "123456789012345",
      |         "accountingPeriodStartDate": "2001-01-01",
      |         "accountingPeriodEndDate": "2001-01-01",
      |         "tradingName": "RCDTS",
      |         "businessAddressDetails": {
      |            "addressLine1": "100 SuttonStreet",
      |            "addressLine2": "Wokingham",
      |            "addressLine3": "Surrey",
      |            "addressLine4": "London",
      |            "postalCode": "DH14EJ",
      |            "countryCode": "GB"
      |         },
      |         "businessContactDetails": {
      |            "phoneNumber": "01332752856",
      |            "mobileNumber": "07782565326",
      |            "faxNumber": "01332754256",
      |            "emailAddress": "stephen@manncorpone.co.uk"
      |         },
      |         "tradingStartDate": "2001-01-01",
      |         "cashOrAccruals": false,
      |         "seasonal": true,
      |         "cessationDate": "2001-01-01",
      |         "cessationReason": "002",
      |         "paperLess": true
      |      }
      |   ]
      |}
      |""".stripMargin)

}
