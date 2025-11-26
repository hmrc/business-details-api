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

package v2.listAllBusinesses

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.HateoasFactory
import api.models.domain.{Nino, TypeOfBusiness}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Result
import routing.Version2
import utils.MockIdGenerator
import v2.listAllBusinesses.model.request.ListAllBusinessesRequestData
import v2.listAllBusinesses.model.response.{Business, ListAllBusinessesResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListAllBusinessesControllerSpec
    extends ControllerBaseSpec(Version2)
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListAllBusinessesService
    with MockListAllBusinessDetailsValidatorFactory
    with MockIdGenerator
    with MockAppConfig {

  private val responseBody = Json.parse(
    s"""
       |{
       |  "listOfBusinesses": [
       |    {
       |      "typeOfBusiness": "self-employment",
       |      "businessId": "123456789012345",
       |      "tradingName": "RCDTS",
       |      "links": [
       |        {
       |          "href": "/individuals/business/details/$nino/123456789012345",
       |          "method": "GET",
       |          "rel": "retrieve-business-details"
       |        }
       |      ]
       |    }
       |  ],
       |  "links": [
       |    {
       |      "href": "/individuals/business/details/$nino/list",
       |      "method": "GET",
       |      "rel": "self"
       |    }
       |  ]
       |}
    """.stripMargin
  )

  private val business     = Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("RCDTS"))
  private val responseData = ListAllBusinessesResponse(Seq(business))
  private val requestData  = ListAllBusinessesRequestData(Nino(nino))

  "handleRequest" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockListAllBusinessesService
          .listAllBusinessesService(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseBody))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockListAllBusinessesService
          .listAllBusinessesService(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller: ListAllBusinessesController = new ListAllBusinessesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockListAllBusinessesService,
      validatorFactory = mockListAllBusinessDetailsValidatorFactory,
      hateoasFactory = new HateoasFactory(mockAppConfig),
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(nino)(fakeGetRequest)
  }

}
