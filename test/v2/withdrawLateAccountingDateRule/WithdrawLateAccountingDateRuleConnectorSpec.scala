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

package v2.withdrawLateAccountingDateRule

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.withdrawLateAccountingDateRule.model.request.WithdrawLateAccountingDateRuleRequest

import java.net.URL
import scala.concurrent.Future

class WithdrawLateAccountingDateRuleConnectorSpec extends ConnectorSpec {

  private val nino: Nino             = Nino("AA123456A")
  private val businessId: BusinessId = BusinessId("XAIS12345678910")
  private val taxYear: TaxYear       = TaxYear.fromMtd("2025-26")

  private val request: WithdrawLateAccountingDateRuleRequest = WithdrawLateAccountingDateRuleRequest(
    nino = nino,
    businessId = businessId,
    taxYear = taxYear
  )

  "WithdrawLateAccountingDateRuleConnector" should {
    "return a successful response" when {
      "the downstream request is successful" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(downstreamUri).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.withdraw(request))
        result shouldBe outcome
      }
    }

    "return an unsuccessful response" when {
      "the downstream request is unsuccessful" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors                 = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willDelete(downstreamUri).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.withdraw(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { self: ConnectorTest =>
    protected val downstreamUri: URL = url"$baseUrl/itsd/income-sources/$nino/late-accounting-date/$businessId?taxYear=${taxYear.asTysDownstream}"

    protected val connector: WithdrawLateAccountingDateRuleConnector = new WithdrawLateAccountingDateRuleConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

}
