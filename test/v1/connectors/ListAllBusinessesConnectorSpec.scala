/*
 * Copyright 2023 HM Revenue & Customs
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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.Nino
import api.models.domain.TypeOfBusiness.`self-employment`
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import play.api.libs.json.Reads
import v1.models.request.listAllBusinesses.ListAllBusinessesRequestData
import v1.models.response.listAllBusiness.ListAllBusinessesResponse.getReads
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesResponse}

import scala.concurrent.Future

class ListAllBusinessesConnectorSpec extends ConnectorSpec {

  private val nino    = Nino("AA123456A")
  private val request = ListAllBusinessesRequestData(nino)

  "listAllBusinessesConnector" must {
    "send a request and return the expected response" in new DesTest with Test {
      val isIfsEnabled: Boolean = false

      MockedAppConfig.featureSwitches
        .returns(
          Configuration(
            "retrieveAdditionalFields.enabled" -> true,
            "ifs.enabled"                      -> isIfsEnabled
          )
        )
        .anyNumberOfTimes()

      val outcome: Right[Nothing, ResponseWrapper[ListAllBusinessesResponse[Business]]] =
        Right(ResponseWrapper(correlationId, ListAllBusinessesResponse(List(Business(`self-employment`, "123456789012345", Some("RCDTS"))))))

      willGet(
        url = s"$baseUrl/registration/business-details/nino/$nino"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[ListAllBusinessesResponse[Business]] = await(connector.listAllBusinesses(request))
      result shouldBe outcome
    }

    "send a request and return the expected response for Release 10" in new IfsTest with Test {
      val isIfsEnabled: Boolean = true

      MockedAppConfig.featureSwitches
        .returns(
          Configuration(
            "retrieveAdditionalFields.enabled" -> true,
            "ifs.enabled"                      -> isIfsEnabled
          )
        )
        .anyNumberOfTimes()

      val outcome: Right[Nothing, ResponseWrapper[ListAllBusinessesResponse[Business]]] =
        Right(ResponseWrapper(correlationId, ListAllBusinessesResponse(List(Business(`self-employment`, "123456789012345", Some("RCDTS"))))))

      willGet(
        url = s"$baseUrl/registration/business-details/nino/$nino"
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[ListAllBusinessesResponse[Business]] = await(connector.listAllBusinesses(request))
      result shouldBe outcome
    }
  }

  trait Test { _: ConnectorTest =>

    protected val connector: ListAllBusinessesConnector = new ListAllBusinessesConnector(mockHttpClient, mockAppConfig)

    protected val isIfsEnabled: Boolean

    implicit protected val responseReads: Reads[ListAllBusinessesResponse[Business]] = getReads(isIfsEnabled)

  }

}
