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

package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockListAllBusinessesConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listAllBusinesses.ListAllBusinessesRequest
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesResponse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class ListAllBusinessesServiceSpec extends UnitSpec {

  private val validNino = Nino("AA123456A")
  private val requestData = ListAllBusinessesRequest(validNino)

  private val responseBody: ListAllBusinessesResponse[Business] = ListAllBusinessesResponse(Seq())

  trait Test extends MockListAllBusinessesConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    val service = new ListAllBusinessesService(
      listAllBusinessesConnector = mockListAllBusinessesConnector
    )
  }

  "service" when {
    "a connector call is successful" should {
      "return a mapped result" in new Test {
        MockListAllBusinessesConnector.listAllBusinesses(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", responseBody))))

        await(service.listAllBusinessesService(requestData)) shouldBe Right(ResponseWrapper("resultId", responseBody))
      }
    }
    "a connector call is unsuccessful" should {
      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $desErrorCode error is returned from the service" in new Test {

          MockListAllBusinessesConnector.listAllBusinesses(requestData)
            .returns(Future.successful(Left(ResponseWrapper("resultId", DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.listAllBusinessesService(requestData)) shouldBe Left(ErrorWrapper(Some("resultId"), error))
        }

      val input = Seq(
        ("INVALID_NINO", NinoFormatError),
        ("INVALID_MTDBSA", DownstreamError),
        ("NOT_FOUND_NINO", NotFoundError),
        ("NOT_FOUND_MTDBSA", DownstreamError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )
      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
