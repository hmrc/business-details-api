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

import api.connectors.DownstreamOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier
import v2.retrieveLateAccountingDateRule.model.request.RetrieveLateAccountingDateRuleRequest
import v2.retrieveLateAccountingDateRule.model.response.RetrieveLateAccountingDateRuleResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveLateAccountingDateRuleConnector extends TestSuite with MockFactory {

  val mockRetrieveLateAccountingDateRuleConnector: RetrieveLateAccountingDateRuleConnector = mock[RetrieveLateAccountingDateRuleConnector]

  object MockRetrieveLateAccountingDateRuleConnector {

    def retrieveLateAccountingDateRule(
        requestData: RetrieveLateAccountingDateRuleRequest): CallHandler[Future[DownstreamOutcome[RetrieveLateAccountingDateRuleResponse]]] = {
      (
        mockRetrieveLateAccountingDateRuleConnector
          .retrieveLateAccountingDateRule(_: RetrieveLateAccountingDateRuleRequest)(
            _: HeaderCarrier,
            _: ExecutionContext,
            _: String
          )
        )
        .expects(requestData, *, *, *)
    }

  }

}
