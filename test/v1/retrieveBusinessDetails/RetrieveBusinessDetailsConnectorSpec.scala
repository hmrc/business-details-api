/*
 * Copyright 2025 HM Revenue & Customs
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

package v1.retrieveBusinessDetails

import api.connectors.ConnectorSpec
import api.models.domain._
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import utils.DateUtils.nowAsUtc
import v1.retrieveBusinessDetails.model.response.downstream.RetrieveBusinessDetailsDownstreamResponse

import scala.concurrent.Future

class RetrieveBusinessDetailsConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")

  private val response: RetrieveBusinessDetailsDownstreamResponse = RetrieveBusinessDetailsDownstreamResponse(Some("2023"), None, None)

  "RetrieveBusinessDetailsConnector" when {
    ".retrieveBusinessDetails" must {
      "send a request and return the expected response when downstream is DES" in new DesTest with Test {
        MockedAppConfig.featureSwitches returns Configuration("ifs_hip_migration_1171.enabled" -> false, "ifs.enabled" -> false)

        private val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(url = s"$baseUrl/registration/business-details/nino/${nino.nino}") returns
          Future.successful(outcome)

        await(connector.retrieveBusinessDetails(nino)) shouldBe outcome
      }

      "send a request and return the expected response when downstream is IFS" in new IfsTest with Test {
        MockedAppConfig.featureSwitches returns Configuration("ifs_hip_migration_1171.enabled" -> false, "ifs.enabled" -> true)

        private val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(url = s"$baseUrl/registration/business-details/nino/${nino.nino}") returns
          Future.successful(outcome)

        await(connector.retrieveBusinessDetails(nino)) shouldBe outcome
      }

      "send a request and return the expected response when downstream is HIP" in new HipTest with Test {
        MockedAppConfig.featureSwitches returns Configuration("ifs_hip_migration_1171.enabled" -> true)

        override def requiredHeaders: Seq[(String, String)] = super.requiredHeaders ++ List(
          "X-Message-Type"        -> "TaxpayerDisplay",
          "X-Originating-System"  -> "MDTP",
          "X-Receipt-Date"        -> nowAsUtc,
          "X-Regime-Type"         -> "ITSA",
          "X-Transmitting-System" -> "HIP"
        )

        private val outcome: Right[Nothing, ResponseWrapper[RetrieveBusinessDetailsDownstreamResponse]] =
          Right(ResponseWrapper(correlationId, response))

        willGet(url = s"$baseUrl/etmp/RESTAdapter/itsa/taxpayer/business-details?nino=${nino.nino}") returns
          Future.successful(outcome)

        await(connector.retrieveBusinessDetails(nino)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector: RetrieveBusinessDetailsConnector =
      new RetrieveBusinessDetailsConnector(http = mockHttpClient, appConfig = mockAppConfig)

  }

}
