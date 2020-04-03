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

package v1.controllers

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockListAllBusinessesRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockListAllBusinessesService, MockMtdIdLookupService}
import v1.models.domain.TypeOfBusiness
import v1.models.errors.{BadRequestError, DownstreamError, ErrorWrapper, MtdError, NinoFormatError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listAllBusinesses.{ListAllBusinessesRawData, ListAllBusinessesRequest}
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListAllBusinessesControllerSpec
  extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockListAllBusinessesService
  with MockListAllBusinessesRequestParser {

    trait Test {
      val hc = HeaderCarrier()

      val controller = new ListAllBusinessesController(
        authService = mockEnrolmentsAuthService,
        lookupService = mockMtdIdLookupService,
        requestDataParser = mockRequestParser,
        service = mockListAllBusinessesService,
        cc = cc
      )
      MockedMtdIdLookupService.lookup(validNino).returns(Future.successful(Right("test-mtd-id")))
      MockedEnrolmentsAuthService.authoriseUser()
    }

    private val validNino = "AA123456A"
    private val correlationId = "X-123"

    private val responseBody = Json.parse(
      """
        |{
        |  "listOfBusinesses":[
        |     {
        |     "typeOfBusiness": "self-employment",
        |     "businessId": "123456789012345",
        |     "tradingName": "RCDTS"
        |     }
        |  ]
        |}
        """.stripMargin
    )

    private val responseData = ListAllBusinessesResponse(Seq(Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("RCDTS"))))

    private val requestData = ListAllBusinessesRequest(Nino(validNino))

    private val rawData = ListAllBusinessesRawData(validNino)


    "handleRequest" should {
      "return OK" when {
        "happy path" in new Test {

          MockListAllBusinessesRequestParser
            .parse(rawData)
            .returns(Right(requestData))

          MockListAllBusinessesService
            .listAllBusinessesService(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

          val result: Future[Result] = controller.handleRequest(validNino)(fakeRequest)

          status(result) shouldBe OK
          contentAsJson(result) shouldBe responseBody
          header("X-CorrelationId", result) shouldBe Some(correlationId)

        }
      }
      "return the error as per spec" when {
        "parser errors occur" must {
          def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
            s"a ${error.code} error is returned from the parser" in new Test {

              MockListAllBusinessesRequestParser
                .parse(rawData)
                .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

              val result: Future[Result] = controller.handleRequest(validNino)(fakeRequest)

              status(result) shouldBe expectedStatus
              contentAsJson(result) shouldBe Json.toJson(error)
              header("X-CorrelationId", result) shouldBe Some(correlationId)
            }
          }

          val input = Seq(
            (NinoFormatError, BAD_REQUEST),
            (BadRequestError, BAD_REQUEST),
            (DownstreamError, INTERNAL_SERVER_ERROR)
          )

          input.foreach(args => (errorsFromParserTester _).tupled(args))
        }

        "service errors occur" must {
          def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
            s"a $mtdError error is returned from the service" in new Test {

              MockListAllBusinessesRequestParser
                .parse(rawData)
                .returns(Right(requestData))

              MockListAllBusinessesService
                .listAllBusinessesService(requestData)
                .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

              val result: Future[Result] = controller.handleRequest(validNino)(fakeRequest)


              status(result) shouldBe expectedStatus
              contentAsJson(result) shouldBe Json.toJson(mtdError)
              header("X-CorrelationId", result) shouldBe Some(correlationId)

            }
          }
          val input = Seq(
            (NinoFormatError, BAD_REQUEST),
            (BadRequestError, BAD_REQUEST),
            (DownstreamError, INTERNAL_SERVER_ERROR),
          )

          input.foreach(args => (serviceErrors _).tupled(args))
        }
      }
    }
}
