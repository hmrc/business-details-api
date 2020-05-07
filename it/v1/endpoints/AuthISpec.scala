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
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino          = "AA123456A"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/$nino/list")
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def desUri: String = s"/registration/business-details/nino/$nino"

    val desResponse: JsValue = Json.parse(
      """
        |{
        |   "safeId": "XE00001234567890",
        |   "nino": "AA123456A",
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
        |         "cashOrAccruals": "cash",
        |         "seasonal": true,
        |         "cessationDate": "2001-01-01",
        |         "cessationReason": "002",
        |         "paperLess": true
        |      }
        |   ]
        |}
        |""".stripMargin)
  }

  "Calling the list endpoint" when {

    "the NINO cannot be converted to a MTD ID" should {

      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.internalServerError(nino)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is authorised" should {

      "return 201" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Status.OK, desResponse)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT logged in" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.FORBIDDEN
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT authorised" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.FORBIDDEN
      }
    }

  }

}
