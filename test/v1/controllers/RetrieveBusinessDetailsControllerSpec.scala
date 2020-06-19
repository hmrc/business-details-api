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
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveBusinessDetailsRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveBusinessDetailsService}
import v1.models.domain.TypeOfBusiness
import v1.models.domain.accountingType.AccountingType
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.GET
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveBusinessDetails.{RetrieveBusinessDetailsRawData, RetrieveBusinessDetailsRequest}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsHateoasData, RetrieveBusinessDetailsResponse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RetrieveBusinessDetailsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveBusinessDetailsService
    with MockHateoasFactory
    with MockRetrieveBusinessDetailsRequestParser {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new RetrieveBusinessDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRequestParser,
      service = mockRetrieveBusinessDetailsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )
    MockedMtdIdLookupService.lookup(validNino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val validNino = "AA123456A"
  private val validBusinessId = "XAIS12345678910"
  private val correlationId = "X-123"
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
    AccountingType.ACCRUALS,
    Some("2016-09-24"),
    Some("2020-03-24"),
    Some("6 Harpic Drive"),
    Some("Domestos Wood"),
    Some("ToiletDucktown"),
    Some("CIFSHIRE"),
    Some("SW4F 3GA"),
    Some("GB")
  )

  private val requestData = RetrieveBusinessDetailsRequest(Nino(validNino), validBusinessId)

  private val rawData = RetrieveBusinessDetailsRawData(validNino, validBusinessId)

  "handleRequest" should {
    "return OK" when {
      "happy path" in new Test {

        MockRetrieveBusinessDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveBusinessDetailsService
          .retrieveBusinessDetailsService(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, RetrieveBusinessDetailsHateoasData(validNino, validBusinessId))
          .returns(HateoasWrapper(responseData, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(validNino, validBusinessId)(fakeRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseBody
        header("X-CorrelationId", result) shouldBe Some(correlationId)

      }
    }
    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveBusinessDetailsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.handleRequest(validNino, validBusinessId)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }
      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveBusinessDetailsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveBusinessDetailsService
              .retrieveBusinessDetailsService(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.handleRequest(validNino, validBusinessId)(fakeRequest)


            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

          }
        }
        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (NoBusinessFoundError, NOT_FOUND)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }

    }
  }
}
