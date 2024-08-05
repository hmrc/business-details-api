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

package api.services

import api.connectors.{MockMtdIdLookupConnector, MtdIdLookupConnector}
import api.models.errors.{ClientOrAgentNotAuthorisedError, InternalError, NinoFormatError, _}
import api.services.MtdIdLookupService.Outcome

import scala.concurrent.Future

class MtdIdLookupServiceSpec extends ServiceSpec {

  trait Test extends MockMtdIdLookupConnector {
    lazy val mtdIdLookupService = new MtdIdLookupService(mockMtdIdLookupConnector)
  }

  val nino                = "AA123456A"
  val invalidNino: String = "not-a-nino"

  "calling getMtdId" when {

    "an mtdId is found for the NINO" should {
      "return the mtdId" in new Test {
        val mtdId = "someMtdId"

        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Right(mtdId))

        await(mtdIdLookupService.lookup(nino)) shouldBe Right(mtdId)
      }
    }

    "an invalid NINO is passed in" should {
      "return a valid mtdId" in new Test {
        val expected: Outcome = Left(NinoFormatError)

        // should not call the connector
        MockedMtdIdLookupConnector
          .lookup(invalidNino)
          .never()

        private val result = await(mtdIdLookupService.lookup(invalidNino))

        result shouldBe expected
      }
    }

    "a not authorised error occurs the service" should {
      "proxy the error to the caller" in new Test {
        val connectorResponse: MtdIdLookupConnector.Outcome = Left(MtdIdLookupConnector.Error(FORBIDDEN))

        MockedMtdIdLookupConnector
          .lookup(nino)
          .returns(Future.successful(connectorResponse))

        val result: MtdIdLookupService.Outcome = await(mtdIdLookupService.lookup(nino))
        result shouldBe Left(ClientOrAgentNotAuthorisedError)
      }
    }

    "a downstream error occurs the service" should {
      "proxy the error to the caller" in new Test {
        val connectorResponse: MtdIdLookupConnector.Outcome = Left(MtdIdLookupConnector.Error(INTERNAL_SERVER_ERROR))

        MockedMtdIdLookupConnector
          .lookup(nino)
          .returns(Future.successful(connectorResponse))

        val result: MtdIdLookupService.Outcome = await(mtdIdLookupService.lookup(nino))
        result shouldBe Left(InternalError)
      }
    }

    "the downstream service returns a 403 status error" should {
      "return ClientOrAgentNotAuthorisedError" in new Test {
        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(FORBIDDEN)))

        await(mtdIdLookupService.lookup(nino)) shouldBe Left(ClientOrAgentNotAuthorisedError)
      }
    }

    "the downstream service returns a 401 status error" should {
      "return InvalidBearerTokenError" in new Test {
        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(UNAUTHORIZED)))

        await(mtdIdLookupService.lookup(nino)) shouldBe Left(InvalidBearerTokenError)
      }
    }

    "the downstream service returns another status code" should {
      "return InternalError" in new Test {
        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(IM_A_TEAPOT)))

        await(mtdIdLookupService.lookup(nino)) shouldBe Left(InternalError)
      }
    }

  }

}
