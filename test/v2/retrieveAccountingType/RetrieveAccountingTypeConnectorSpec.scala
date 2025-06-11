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

package v2.retrieveAccountingType

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import v2.common.models.AccountingType
import v2.retrieveAccountingType.model.request._
import v2.retrieveAccountingType.model.response.RetrieveAccountingTypeResponse

import scala.concurrent.Future

class RetrieveAccountingTypeConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2024-25")

  private val request  = RetrieveAccountingTypeRequest(nino, businessId, taxYear)
  private val response = RetrieveAccountingTypeResponse(AccountingType.CASH)

  val queryParams = Map(
    "incomeSourceId"  -> "XAIS12345678910",
    "taxYearExplicit" -> "2025"
  )

  val mappedQueryParams: Map[String, String] = queryParams.collect { case (k: String, v: String) => (k, v) }

  "RetrieveAccountingTypeConnector" must {
    "return a successful response" when {
      List(
        (false, None),
        (true, Some("ACCOUNTING_TYPE"))
      ).foreach { case (passIntentHeaderFlag, intentValue) =>
        s"the downstream request is successful and passIntentHeader is set to $passIntentHeaderFlag" in new HipTest with Test {
          override def intent: Option[String] = intentValue

          val outcome: Right[Nothing, ResponseWrapper[RetrieveAccountingTypeResponse]] = Right(ResponseWrapper(correlationId, response))

          MockedAppConfig.featureSwitches returns Configuration("passIntentHeader.enabled" -> passIntentHeaderFlag)

          willGet(s"$baseUrl/itsd/income-sources/v2/$nino", mappedQueryParams.toList).returns(Future.successful(outcome))

          val result: DownstreamOutcome[RetrieveAccountingTypeResponse] = await(connector.retrieveAccountingType(request))
          result shouldBe outcome
        }
      }
    }

    "return an unsuccessful response" when {
      List(
        (false, None),
        (true, Some("ACCOUNTING_TYPE"))
      ).foreach { case (passIntentHeaderFlag, intentValue) =>
        s"the downstream request is unsuccessful and passIntentHeader is set to $passIntentHeaderFlag" in new HipTest with Test {
          override def intent: Option[String] = intentValue

          val downstreamErrorResponse: DownstreamErrors                 = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
          val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

          MockedAppConfig.featureSwitches returns Configuration("passIntentHeader.enabled" -> passIntentHeaderFlag)

          willGet(s"$baseUrl/itsd/income-sources/v2/$nino", mappedQueryParams.toList).returns(Future.successful(outcome))

          val result: DownstreamOutcome[RetrieveAccountingTypeResponse] = await(connector.retrieveAccountingType(request))
          result shouldBe outcome
        }
      }
    }
  }

  trait Test { _: ConnectorTest =>
    protected val connector: RetrieveAccountingTypeConnector = new RetrieveAccountingTypeConnector(mockHttpClient, mockAppConfig)
  }

}
