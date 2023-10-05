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

package v1.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.mocks.MockIdGenerator
import api.models.domain.{Nino, TypeOfBusiness}
import api.models.errors._
import api.hateoas.Method.GET
import play.api.libs.json.Json
import play.api.mvc.Result
import v1.models.request.listAllBusinesses.{ListAllBusinessesRawData, ListAllBusinessesRequest}
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesHateoasData, ListAllBusinessesResponse}
import api.models.outcomes.ResponseWrapper
import api.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.controllers.requestParsers.MockListAllBusinessesRequestParser
import v1.services.MockListAllBusinessesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListAllBusinessesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListAllBusinessesService
    with MockHateoasFactory
    with MockListAllBusinessesRequestParser
    with MockIdGenerator {

  private val validNino            = "AA123456A"
  private val testHateoasLink      = Link(href = "/foo/bar", method = GET, rel = "test-relationship")
  private val testInnerHateoasLink = Link(href = "/foo/bar", method = GET, rel = "test-inner-relationship")

  private val responseBody = Json.parse(
    """
      |{
      |  "listOfBusinesses":[
      |    {
      |      "typeOfBusiness": "self-employment",
      |      "businessId": "123456789012345",
      |      "tradingName": "RCDTS",
      |      "links": [
      |        {
      |          "href": "/foo/bar",
      |          "method": "GET",
      |          "rel": "test-inner-relationship"
      |        }
      |      ]
      |    }
      |  ],
      |   "links": [
      |     {
      |       "href": "/foo/bar",
      |       "method": "GET",
      |       "rel": "test-relationship"
      |     }
      |   ]
      |}
        """.stripMargin
  )

  private val business     = Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("RCDTS"))
  private val responseData = ListAllBusinessesResponse(Seq(business))

  val hateoasResponse: ListAllBusinessesResponse[HateoasWrapper[Business]] = ListAllBusinessesResponse(
    Seq(HateoasWrapper(business, Seq(testInnerHateoasLink))))

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

        MockHateoasFactory
          .wrapList(responseData, ListAllBusinessesHateoasData(validNino))
          .returns(HateoasWrapper(hateoasResponse, Seq(testHateoasLink)))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseBody))

      }
    }
    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockListAllBusinessesRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)

      }

      "the service returns an error" in new Test {
        MockListAllBusinessesRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockListAllBusinessesService
          .listAllBusinessesService(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new ListAllBusinessesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockListAllBusinessesService,
      parser = mockRequestParser,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino)(fakeGetRequest)

  }

}
