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

package v2.updateAccountingType

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import v2.common.models.AccountingType
import v2.updateAccountingType.def1.model.request._

import scala.concurrent.Future

class UpdateAccountingTypeConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2024-25")
  private val body       = Def1_UpdateAccountingTypeRequestBody(AccountingType.`CASH`)

  private val request = Def1_UpdateAccountingTypeRequestData(nino, businessId, taxYear, body)

  "retrieveBusinessDetailsConnector" must {
    "return a successful response" when {
      "the downstream request is successful" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willPut(s"$baseUrl/itsd/income-sources/$nino/accounting-type/$businessId", body).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.update(request))
        result shouldBe outcome
      }
    }

    "return an unsuccessful response" when {
      "the downstream request is unsuccessful" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors                 = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willPut(s"$baseUrl/itsd/income-sources/$nino/accounting-type/$businessId", body).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.update(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    protected val connector: UpdateAccountingTypeConnector = new UpdateAccountingTypeConnector(mockHttpClient, mockAppConfig)
  }

}
