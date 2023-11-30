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
import api.models.domain._
import api.models.outcomes.ResponseWrapper
import config.MockFeatureSwitches
import play.api.Configuration
import play.api.libs.json.Reads
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRequestData
import v1.models.response.retrieveBusinessDetails.downstream.{LatencyDetails, LatencyIndicator, RetrieveBusinessDetailsDownstreamResponse}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}

import scala.concurrent.Future

class RetrieveBusinessDetailsConnectorSpec extends ConnectorSpec with MockFeatureSwitches {

  private val nino = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val request = RetrieveBusinessDetailsRequestData(nino, businessId)

  // @formatter:off
  private val response: RetrieveBusinessDetailsResponse = RetrieveBusinessDetailsResponse(
    "XAIS12345678910", TypeOfBusiness.`self-employment`, None, List(AccountingPeriod("2018-04-06", "2019-04-05")),
    AccountingType.CASH, None, None, None, None, None, None, None, None, Some("2018-04-06"), Some("2018-12-12"),
    Some(LatencyDetails("2018-12-12", TaxYear.fromDownstream("2018"), LatencyIndicator.Annual, TaxYear.fromDownstream("2019"), LatencyIndicator.Quarterly)),
    Some("2023")
  )
  // @formatter:on

  MockFeatureSwitches.isIfsEnabled.returns(false).anyNumberOfTimes()

  implicit private val responseReads: Reads[RetrieveBusinessDetailsDownstreamResponse] = RetrieveBusinessDetailsDownstreamResponse.reads

  "retrieveBusinessDetailsConnector" must {
    "send a request and return the expected response" in new DesTest with Test {
      MockedAppConfig.featureSwitches returns Configuration("ifs.enabled" -> false)

      val outcome: Right[Nothing, ResponseWrapper[Seq[RetrieveBusinessDetailsResponse]]] = Right(ResponseWrapper(correlationId, Seq(response)))

      willGet(
        url = s"$baseUrl/registration/business-details/nino/${request.nino}"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[RetrieveBusinessDetailsDownstreamResponse] = await(connector.retrieveBusinessDetails(request))
      result shouldBe outcome
    }

    "send a request and return the expected response when ifs feature switch is enabled" in new IfsTest with Test {
      MockedAppConfig.featureSwitches returns Configuration("ifs.enabled" -> true)

      val outcome: Right[Nothing, ResponseWrapper[Seq[RetrieveBusinessDetailsResponse]]] = Right(ResponseWrapper(correlationId, Seq(response)))

      willGet(
        url = s"$baseUrl/registration/business-details/nino/${request.nino}"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[RetrieveBusinessDetailsDownstreamResponse] = await(connector.retrieveBusinessDetails(request))
      result shouldBe outcome
    }
  }

  trait Test {
    _: ConnectorTest =>
    protected val connector: RetrieveBusinessDetailsConnector = new RetrieveBusinessDetailsConnector(http = mockHttpClient, appConfig = mockAppConfig)
  }

}
