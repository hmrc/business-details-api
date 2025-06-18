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

package v2.retrievePeriodsOfAccount

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.common.models.PeriodsOfAccountDates
import v2.retrievePeriodsOfAccount.model.request._
import v2.retrievePeriodsOfAccount.model.response.RetrievePeriodsOfAccountResponse

import scala.concurrent.Future

class RetrievePeriodsOfAccountConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2024-25")

  private val request = RetrievePeriodsOfAccountRequest(nino, businessId, taxYear)

  private val response = RetrievePeriodsOfAccountResponse(
    Timestamp("2019-08-24T14:15:22Z"),
    true,
    Some(
      Seq(
        PeriodsOfAccountDates("2025-04-06", "2025-07-05"),
        PeriodsOfAccountDates("2025-07-06", "2025-10-05"),
        PeriodsOfAccountDates("2025-10-06", "2025-01-05"),
        PeriodsOfAccountDates("2025-01-06", "2025-04-05")
      )
    )
  )

  val queryParams = Map(
    "taxYear" -> "24-25"
  )

  val mappedQueryParams: Map[String, String] = queryParams.collect { case (k: String, v: String) => (k, v) }

  "retrievePeriodsOfAccountConnector" must {
    "return a successful response" when {
      "the downstream request is successful" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrievePeriodsOfAccountResponse]] = Right(ResponseWrapper(correlationId, response))

        willGet(url"$baseUrl/itsd/income-sources/$nino/periods-of-account/$businessId", mappedQueryParams.toList).returns(Future.successful(outcome))

        val result: DownstreamOutcome[RetrievePeriodsOfAccountResponse] = await(connector.retrievePeriodsOfAccount(request))
        result shouldBe outcome
      }
    }

    "return an unsuccessful response" when {
      "the downstream request is unsuccessful" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors                 = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willGet(url"$baseUrl/itsd/income-sources/$nino/periods-of-account/$businessId", mappedQueryParams.toList).returns(Future.successful(outcome))

        val result: DownstreamOutcome[RetrievePeriodsOfAccountResponse] = await(connector.retrievePeriodsOfAccount(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    protected val connector: RetrievePeriodsOfAccountConnector = new RetrievePeriodsOfAccountConnector(mockHttpClient, mockAppConfig)
  }

}
