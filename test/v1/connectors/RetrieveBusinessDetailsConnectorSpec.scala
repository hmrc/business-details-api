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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TypeOfBusiness}
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRequest
import v1.models.response.retrieveBusinessDetails.downstream.{LatencyDetails, LatencyIndicator, RetrieveBusinessDetailsDownstreamResponse}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}

import scala.concurrent.Future

class RetrieveBusinessDetailsConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = "XAIS12345678910"

  "retrieveBusinessDetailsConnector" must {
    "send a request and return the expected response" in new DesTest with Test {
      val outcome: Right[Nothing, ResponseWrapper[Seq[RetrieveBusinessDetailsResponse]]] = Right(ResponseWrapper(correlationId, Seq(response)))

      willGet(
        url = s"$baseUrl/registration/business-details/nino/${request.nino.nino}"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[RetrieveBusinessDetailsDownstreamResponse] = await(connector.retrieveBusinessDetails(request))
      result shouldBe outcome
    }

    "send a request and return the expected response when r10-IFS feature switch is enabled" in new IfsTest with Test {
      val outcome: Right[Nothing, ResponseWrapper[Seq[RetrieveBusinessDetailsResponse]]] = Right(ResponseWrapper(correlationId, Seq(response)))

      willGet(
        url = s"$baseUrl/registration/business-details/nino/${request.nino.nino}"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[RetrieveBusinessDetailsDownstreamResponse] = await(connector.retrieveBusinessDetails(request))
      result shouldBe outcome
    }
  }

  trait Test { _: ConnectorTest =>
    protected val connector: RetrieveBusinessDetailsConnector = new RetrieveBusinessDetailsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    protected val request: RetrieveBusinessDetailsRequest = RetrieveBusinessDetailsRequest(nino, businessId)

    protected val response: RetrieveBusinessDetailsResponse = RetrieveBusinessDetailsResponse(
      "XAIS12345678910",
      TypeOfBusiness.`self-employment`,
      None,
      Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      Some("2018-04-06"),
      Some("2018-12-12"),
      Some(LatencyDetails("2018-12-12", "2018", LatencyIndicator.Annual, "2019", LatencyIndicator.Quarterly)),
      Some("2023")
    )

  }

}
