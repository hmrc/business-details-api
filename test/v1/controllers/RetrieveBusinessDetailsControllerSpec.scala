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
import api.mocks.{MockAppConfig, MockIdGenerator}
import api.models.domain.accountingType.AccountingType
import api.models.domain.{Nino, TypeOfBusiness}
import api.models.errors._
import api.hateoas.Method.GET
import api.models.outcomes.ResponseWrapper
import api.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Result
import v1.controllers.requestParsers.MockRetrieveBusinessDetailsRequestParser
import v1.models.request.retrieveBusinessDetails.{RetrieveBusinessDetailsRawData, RetrieveBusinessDetailsRequest}
import v1.models.response.retrieveBusinessDetails.downstream.{LatencyDetails, LatencyIndicator}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsHateoasData, RetrieveBusinessDetailsResponse}
import v1.services.MockRetrieveBusinessDetailsService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveBusinessDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveBusinessDetailsService
    with MockHateoasFactory
    with MockRetrieveBusinessDetailsRequestParser
    with MockIdGenerator
    with MockAppConfig {

  private val businessId      = "XAIS12345678910"
  private val testHateoasLink = Link(href = "/foo/bar", method = GET, rel = "test-relationship")

  private val responseBody = Json.parse(
    """
      |{
      |   "businessId": "XAIS12345678910",
      |   "typeOfBusiness": "self-employment",
      |   "tradingName": "Aardvark Window Cleaning Services",
      |   "accountingPeriods": [
      |     {
      |       "start": "2018-04-06",
      |       "end": "2019-04-05"
      |     }
      |   ],
      |   "accountingType": "ACCRUALS",
      |   "commencementDate": "2016-09-24",
      |   "cessationDate": "2020-03-24",
      |   "businessAddressLineOne": "6 Harpic Drive",
      |   "businessAddressLineTwo": "Domestos Wood",
      |   "businessAddressLineThree": "ToiletDucktown",
      |   "businessAddressLineFour": "CIFSHIRE",
      |   "businessAddressPostcode": "SW4F 3GA",
      |   "businessAddressCountryCode": "GB",
      |   "firstAccountingPeriodStartDate": "2018-04-06",
      |   "firstAccountingPeriodEndDate": "2018-12-12",
      |   "latencyDetails": {
      |     "latencyEndDate": "2018-12-12",
      |     "taxYear1": "2017-18",
      |     "latencyIndicator1": "A",
      |     "taxYear2": "2018-19",
      |     "latencyIndicator2": "Q"
      |   },
      |   "yearOfMigration": "2023",
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

  private val responseData = RetrieveBusinessDetailsResponse(
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
    Some("GB"),
    Some("2018-04-06"),
    Some("2018-12-12"),
    Some(LatencyDetails("2018-12-12", "2017-18", LatencyIndicator.Annual, "2018-19", LatencyIndicator.Quarterly)),
    Some("2023")
  )

  private val requestData = RetrieveBusinessDetailsRequest(Nino(nino), businessId)

  private val rawData = RetrieveBusinessDetailsRawData(nino, businessId)

  "handleRequest" should {
    "return successful response with status OK" when {
      "valid request" in new Test {

        MockRetrieveBusinessDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveBusinessDetailsService
          .retrieveBusinessDetailsService(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, RetrieveBusinessDetailsHateoasData(nino, businessId))
          .returns(HateoasWrapper(responseData, Seq(testHateoasLink)))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseBody))

      }
    }
    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveBusinessDetailsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveBusinessDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveBusinessDetailsService
          .retrieveBusinessDetailsService(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }

    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveBusinessDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRequestParser,
      service = mockRetrieveBusinessDetailsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockAppConfig.featureSwitches
      .returns(Configuration("retrieveAdditionalFields.enabled" -> true))
      .anyNumberOfTimes()

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId)(fakeGetRequest)

  }

}
