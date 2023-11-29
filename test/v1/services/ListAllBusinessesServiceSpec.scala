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

package v1.services

import api.models.domain.Nino
import api.models.errors.{
  DownstreamErrorCode,
  DownstreamErrors,
  ErrorWrapper,
  InternalError,
  MtdError,
  NinoFormatError,
  NotFoundError,
  RuleIncorrectGovTestScenarioError
}
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import config.MockAppConfig
import play.api.Configuration
import v1.connectors.MockListAllBusinessesConnector
import v1.models.request.listAllBusinesses.ListAllBusinessesRequestData
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesResponse}

import scala.concurrent.Future

class ListAllBusinessesServiceSpec extends ServiceSpec with MockAppConfig {

  private val validNino    = Nino("AA123456A")
  private val requestData  = ListAllBusinessesRequestData(validNino)
  private val responseBody = ListAllBusinessesResponse(List[Business]())

  "service" when {
    "a connector call is successful" should {
      "return a mapped result" in new Test {
        MockedListAllBusinessesConnector
          .listAllBusinesses(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        val result: ServiceOutcome[ListAllBusinessesResponse[Business]] = await(service.listAllBusinessesService(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, responseBody))
      }
    }
    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test {

          MockedListAllBusinessesConnector
            .listAllBusinesses(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[ListAllBusinessesResponse[Business]] = await(service.listAllBusinessesService(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("INVALID_NINO", NinoFormatError),
        ("INVALID_MTDBSA", InternalError),
        ("UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError),
        ("NOT_FOUND_NINO", NotFoundError),
        ("NOT_FOUND_MTDBSA", InternalError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      val extraIfsErrors = List(
        ("INVALID_MTD_ID", InternalError),
        ("INVALID_CORRELATIONID", InternalError),
        ("INVALID_IDTYPE", InternalError),
        ("NOT_FOUND", NotFoundError)
      )

      (errors ++ extraIfsErrors).foreach((serviceError _).tupled)
    }
  }

  private trait Test extends MockListAllBusinessesConnector {

    MockedAppConfig.featureSwitches
      .returns(Configuration("retrieveAdditionalFields.enabled" -> true))
      .anyNumberOfTimes()

    protected val service = new ListAllBusinessesService(mockListAllBusinessesConnector, mockAppConfig)

  }

}
