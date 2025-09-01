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

package v2.retrieveLateAccountingDateRule

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors}
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import uk.gov.hmrc.http.StringContextOps
import v2.retrieveLateAccountingDateRule.model.request.RetrieveLateAccountingDateRuleRequest
import v2.retrieveLateAccountingDateRule.model.response.RetrieveLateAccountingDateRuleResponse

import scala.concurrent.Future

class RetrieveLateAccountingDateRuleConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2024-25")

  private val request = RetrieveLateAccountingDateRuleRequest(nino, businessId, taxYear)

  private val response = RetrieveLateAccountingDateRuleResponse(disapply = true, eligible = true, Some(TaxYear("2025")), Some(TaxYear("2025")))

  val queryParams = Map(
    "incomeSourceId"  -> "XAIS12345678910",
    "taxYearExplicit" -> "2024-25"
  )

  val mappedQueryParams: Map[String, String] = queryParams.collect { case (k: String, v: String) => (k, v) }

  "RetrieveAccountingTypeConnector" must {
    "return a successful response" when {
      List(
        (false, None),
        (true, Some("LATE_ACCOUNTING_DATE_RULE"))
      ).foreach { case (passIntentHeaderFlag, intentValue) =>
        s"the downstream request is successful and passIntentHeader is set to $passIntentHeaderFlag" in new HipTest with Test {
          override def intent: Option[String] = intentValue

          val outcome: Right[Nothing, ResponseWrapper[RetrieveLateAccountingDateRuleResponse]] = Right(ResponseWrapper(correlationId, response))

          MockedAppConfig.featureSwitches returns Configuration("passIntentHeader.enabled" -> passIntentHeaderFlag)

          willGet(url"$baseUrl/itsd/income-sources/v2/$nino", mappedQueryParams.toList).returns(Future.successful(outcome))

          val result: DownstreamOutcome[RetrieveLateAccountingDateRuleResponse] = await(connector.retrieveLateAccountingDateRule(request))
          result shouldBe outcome
        }
      }
    }

    "return an unsuccessful response" when {
      List(
        (false, None),
        (true, Some("LATE_ACCOUNTING_DATE_RULE"))
      ).foreach { case (passIntentHeaderFlag, intentValue) =>
        s"the downstream request is unsuccessful and passIntentHeader is set to $passIntentHeaderFlag" in new HipTest with Test {
          override def intent: Option[String] = intentValue

          val downstreamErrorResponse: DownstreamErrors                 = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
          val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

          MockedAppConfig.featureSwitches returns Configuration("passIntentHeader.enabled" -> passIntentHeaderFlag)

          willGet(url"$baseUrl/itsd/income-sources/v2/$nino", mappedQueryParams.toList).returns(Future.successful(outcome))

          val result: DownstreamOutcome[RetrieveLateAccountingDateRuleResponse] = await(connector.retrieveLateAccountingDateRule(request))
          result shouldBe outcome
        }
      }
    }
  }

  trait Test { _: ConnectorTest =>
    protected val connector: RetrieveLateAccountingDateRuleConnector = new RetrieveLateAccountingDateRuleConnector(mockHttpClient, mockAppConfig)
  }

}
