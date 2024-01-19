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

package v1.connectors

import api.connectors.ConnectorSpec
import api.models.domain._
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import v1.models.response.downstream.retrieveBusinessDetails.RetrieveBusinessDetailsDownstreamResponse

import scala.concurrent.Future

class RetrieveBusinessDetailsConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")

  private val response: RetrieveBusinessDetailsDownstreamResponse = RetrieveBusinessDetailsDownstreamResponse(Some("2023"), None, None)

  "retrieveBusinessDetailsConnector" must {
    "send a request and return the expected response" in new DesTest with Test {
      MockedAppConfig.featureSwitches returns Configuration("ifs.enabled" -> false)

      private val outcome = Right(ResponseWrapper(correlationId, response))

      willGet(url = s"$baseUrl/registration/business-details/nino/${nino.nino}") returns
        Future.successful(outcome)

      await(connector.retrieveBusinessDetails(nino)) shouldBe outcome
    }

    "send a request and return the expected response when ifs feature switch is enabled" in new IfsTest with Test {
      MockedAppConfig.featureSwitches returns Configuration("ifs.enabled" -> true)

      private val outcome = Right(ResponseWrapper(correlationId, response))

      willGet(url = s"$baseUrl/registration/business-details/nino/${nino.nino}") returns
        Future.successful(outcome)

      await(connector.retrieveBusinessDetails(nino)) shouldBe outcome
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector: RetrieveBusinessDetailsConnector =
      new RetrieveBusinessDetailsConnector(http = mockHttpClient, appConfig = mockAppConfig)

  }

}
