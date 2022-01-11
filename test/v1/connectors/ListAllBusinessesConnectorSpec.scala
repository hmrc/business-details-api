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

package v1.connectors

import mocks.MockAppConfig
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.domain.TypeOfBusiness.`self-employment`
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listAllBusinesses.ListAllBusinessesRequest
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesResponse}

import scala.concurrent.Future

class ListAllBusinessesConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")

  class Test extends MockHttpClient with MockAppConfig {
    val connector: ListAllBusinessesConnector = new ListAllBusinessesConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "list all businesses" should {
    val request = ListAllBusinessesRequest(nino)
    "return a result" when {
      "the downstream call is successful" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ListAllBusinessesResponse(Seq(Business(`self-employment`, "123456789012345", Some("RCDTS"))))))
        MockedHttpClient.
          get(
            url = s"$baseUrl/registration/business-details/nino/${request.nino.nino}",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.successful(outcome))
        await(connector.listAllBusinesses(request)) shouldBe outcome
      }
    }
  }
}
