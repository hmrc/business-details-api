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

package v2.createUpdatePeriodsOfAccount

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequest
import v2.fixtures.CreateUpdatePeriodsOfAccountFixtures.minimumRequestBodyModel

import scala.concurrent.Future

class CreateUpdatePeriodsOfAccountConnectorSpec extends ConnectorSpec {

  private val nino: Nino             = Nino("AA123456A")
  private val businessId: BusinessId = BusinessId("XAIS12345678910")
  private val taxYear: TaxYear       = TaxYear.fromMtd("2025-26")

  private val request: CreateUpdatePeriodsOfAccountRequest = CreateUpdatePeriodsOfAccountRequest(
    nino = nino,
    businessId = businessId,
    taxYear = taxYear,
    body = minimumRequestBodyModel
  )

  "CreateUpdatePeriodsOfAccountConnector" should {
    "return a successful response" when {
      "the downstream request is successful" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willPut(downstreamUri, minimumRequestBodyModel).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createUpdate(request))
        result shouldBe outcome
      }
    }

    "return an unsuccessful response" when {
      "the downstream request is unsuccessful" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors                 = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willPut(downstreamUri, minimumRequestBodyModel).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createUpdate(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    protected val downstreamUri: String = s"$baseUrl/itsd/income-sources/$nino/periods-of-account/$businessId?taxYear=${taxYear.asTysDownstream}"

    protected val connector: CreateUpdatePeriodsOfAccountConnector = new CreateUpdatePeriodsOfAccountConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

}
