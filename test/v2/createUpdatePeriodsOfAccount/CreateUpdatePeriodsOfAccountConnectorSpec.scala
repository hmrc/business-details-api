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

import api.connectors.ConnectorSpec
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import v2.common.models.PeriodsOfAccountDates
import v2.createUpdatePeriodsOfAccount.request.{CreateUpdatePeriodsOfAccountRequestBody, CreateUpdatePeriodsOfAccountRequestData}

import scala.concurrent.Future

class CreateUpdatePeriodsOfAccountConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2024-25")
  private val body       = CreateUpdatePeriodsOfAccountRequestBody(true, Some(Seq(PeriodsOfAccountDates("2024-04-06", "2025-04-05"))))
  private val request    = CreateUpdatePeriodsOfAccountRequestData(nino.nino, businessId, taxYear, body)

  "CreateUpdatePeriodsOfAccountConnector" should {
    "return a successful response" when {
      "the downstream request is successful" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willPut(downStreamUri, body)
          .returns(Future.successful(outcome))

        val result = await(connector.create(request))
        result shouldBe outcome
      }
    }

    "return an unsuccessful response" when {
      "the downstream request is unsuccessful" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors                 = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willPut(downStreamUri, body)
          .returns(Future.successful(outcome))

        val result = await(connector.create(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    protected val downStreamUri = s"$baseUrl/itsd/income-sources/$nino/periods-of-account/$businessId?taxYear=${taxYear.asTysDownstream}"
    protected val connector: CreateUpdatePeriodsOfAccountConnector = new CreateUpdatePeriodsOfAccountConnector(mockHttpClient, mockAppConfig)
  }

}
