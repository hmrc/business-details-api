/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.connectors

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.domain.TypeOfBusiness
import v1.models.domain.accountingType.AccountingType
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRequest
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}

import scala.concurrent.Future

class RetrieveBusinessDetailsConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")
  private val businessId = "XAIS12345678910"

  class Test extends MockHttpClient with MockAppConfig {
    val connector: RetrieveBusinessDetailsConnector = new RetrieveBusinessDetailsConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "retrieve business details" should {
    val request = RetrieveBusinessDetailsRequest(nino, businessId)
    "return a result" when {
      "the downstream call is successful" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, Seq(RetrieveBusinessDetailsResponse(
          "XAIS12345678910",
          TypeOfBusiness.`self-employment`,
          Some("Aardvark Window Cleaning Services"),
          Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
          Some(AccountingType.ACCRUALS),
          Some("2016-09-24"),
          Some("2020-03-24"),
          Some("6 Harpic Drive"),
          Some("Domestos Wood"),
          Some("ToiletDucktown"),
          Some("CIFSHIRE"),
          Some("SW4F 3GA"),
          Some("GB")
        ))))
        MockedHttpClient.
          get(
            url = s"$baseUrl/registration/business-details/nino/${request.nino}",
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          ).returns(Future.successful(outcome))
        await(connector.retrieveBusinessDetails(request)) shouldBe outcome
      }
    }
  }

}
