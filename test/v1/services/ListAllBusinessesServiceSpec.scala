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

import api.controllers.EndpointLogContext
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
import api.services.ServiceSpec
import mocks.MockAppConfig
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.connectors.MockListAllBusinessesConnector
import v1.models.request.listAllBusinesses.ListAllBusinessesRequest
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesResponse}

import scala.concurrent.Future

class ListAllBusinessesServiceSpec extends ServiceSpec with MockAppConfig {

  private val validNino   = Nino("AA123456A")
  private val requestData = ListAllBusinessesRequest(validNino)

  private val responseBody: ListAllBusinessesResponse[Business] = ListAllBusinessesResponse(Seq())

  trait Test extends MockListAllBusinessesConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    val isEnabled: Boolean                      = true

    MockAppConfig.featureSwitches
      .returns(Configuration("retrieveAdditionalFields.enabled" -> isEnabled))
      .anyNumberOfTimes()

    val service = new ListAllBusinessesService(
      connector = mockListAllBusinessesConnector,
      mockAppConfig
    )

  }

  "service" when {
    "a connector call is successful" should {
      "return a mapped result" in new Test {
        MockListAllBusinessesConnector
          .listAllBusinesses(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        await(service.listAllBusinessesService(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseBody))
      }
    }
    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test {

          MockListAllBusinessesConnector
            .listAllBusinesses(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.listAllBusinessesService(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Seq(
        ("INVALID_NINO", NinoFormatError),
        ("INVALID_MTDBSA", InternalError),
        ("UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError),
        ("NOT_FOUND_NINO", NotFoundError),
        ("NOT_FOUND_MTDBSA", InternalError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      val extraIfsErrors = Seq(
        ("INVALID_MTD_ID", InternalError),
        ("INVALID_CORRELATIONID", InternalError),
        ("INVALID_IDTYPE", InternalError),
        ("NOT_FOUND", NotFoundError)
      )

      (errors ++ extraIfsErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
