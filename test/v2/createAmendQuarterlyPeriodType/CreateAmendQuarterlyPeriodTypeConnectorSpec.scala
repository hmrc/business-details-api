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

package v2.createAmendQuarterlyPeriodType

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v2.createAmendQuarterlyPeriodType.def1.model.request._

import scala.concurrent.Future

class CreateAmendQuarterlyPeriodTypeConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2023-24")
  private val body       = Def1_CreateAmendQuarterlyPeriodTypeRequestBody(QuarterlyPeriodType.`standard`)

  private val request = Def1_CreateAmendQuarterlyPeriodTypeRequestData(nino, businessId, taxYear, body)

  "retrieveBusinessDetailsConnector" must {
    "return a successful response" when {
      "the downstream request is successful" in new Api2089Test with Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willPut(url"$baseUrl/income-tax/23-24/income-sources/reporting-type/$nino/$businessId", body).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.create(request))
        result shouldBe outcome
      }
    }

    "return an unsuccessful response" when {
      "the downstream request is unsuccessful" in new Api2089Test with Test {
        val downstreamErrorResponse: DownstreamErrors                 = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willPut(url"$baseUrl/income-tax/23-24/income-sources/reporting-type/$nino/$businessId", body).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.create(request))
        result shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    protected val connector: CreateAmendQuarterlyPeriodTypeConnector = new CreateAmendQuarterlyPeriodTypeConnector(mockHttpClient, mockAppConfig)
  }

}
