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
import v1.mocks.connectors.MockRetrieveBusinessDetailsConnector
import v1.models.domain.TypeOfBusiness
import v1.models.domain.accountingType.AccountingType
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRequest
import v1.models.response.retrieveBusinessDetails.des.{BusinessDetails, RetrieveBusinessDetailsDesResponse}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RetrieveBusinessDetailsServiceSpec extends UnitSpec {

  private val validNino = Nino("AA123456A")
  private val validId = "XAIS12345678910"
  private val requestData = RetrieveBusinessDetailsRequest(validNino, validId)
  private val badRequestData = RetrieveBusinessDetailsRequest(validNino, "SDFG3456782190")

  private val responseBody = RetrieveBusinessDetailsResponse(
    "XAIS12345678910",
    TypeOfBusiness.`self-employment`,
    Some("Aardvark Window Cleaning Services"),
    Some(Seq(AccountingPeriod("2018-04-06", "2019-04-05"))),
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

  private val desSingleResponseBody = RetrieveBusinessDetailsDesResponse(Seq(BusinessDetails(
    "XAIS12345678910",
    TypeOfBusiness.`self-employment`,
    Some("Aardvark Window Cleaning Services"),
    Some(Seq(AccountingPeriod("2018-04-06", "2019-04-05"))),
    AccountingType.ACCRUALS,
    Some("2016-09-24"),
    Some("2020-03-24"),
    Some("6 Harpic Drive"),
    Some("Domestos Wood"),
    Some("ToiletDucktown"),
    Some("CIFSHIRE"),
    Some("SW4F 3GA"),
    Some("GB")
  )))

  private val desMultiResponseBody = RetrieveBusinessDetailsDesResponse(Seq(BusinessDetails(
    "XAIS12345678910",
    TypeOfBusiness.`self-employment`,
    Some("Aardvark Window Cleaning Services"),
    Some(Seq(AccountingPeriod("2018-04-06", "2019-04-05"))),
    AccountingType.ACCRUALS,
    Some("2016-09-24"),
    Some("2020-03-24"),
    Some("6 Harpic Drive"),
    Some("Domestos Wood"),
    Some("ToiletDucktown"),
    Some("CIFSHIRE"),
    Some("SW4F 3GA"),
    Some("GB")
  ),
    BusinessDetails(
    "XAIS0987654321",
    TypeOfBusiness.`self-employment`,
    Some("Aardvark Window Cleaning Services"),
    Some(Seq(AccountingPeriod("2018-04-06", "2019-04-05"))),
    AccountingType.ACCRUALS,
    Some("2016-09-24"),
    Some("2020-03-24"),
    Some("6 Harpic Drive"),
    Some("Domestos Wood"),
    Some("ToiletDucktown"),
    Some("CIFSHIRE"),
    Some("SW4F 3GA"),
    Some("GB")
  )))

  trait Test extends MockRetrieveBusinessDetailsConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    val service = new RetrieveBusinessDetailsService(
      retrieveBusinessDetailsConnector = mockRetrieveBusinessDetailsConnector
    )
  }

  "service" when {
    "a connector call is successful" should {
      "return a mapped result from a single des response" in new Test {
        MockRetrieveBusinessDetailsConnector.retrieveBusinessDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", desSingleResponseBody))))

        await(service.retrieveBusinessDetailsService(requestData)) shouldBe Right(ResponseWrapper("resultId", responseBody))
      }
      "return a mapped result from multiple des responses" in new Test {
        MockRetrieveBusinessDetailsConnector.retrieveBusinessDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", desMultiResponseBody))))

        await(service.retrieveBusinessDetailsService(requestData)) shouldBe Right(ResponseWrapper("resultId", responseBody))
      }
    }
    "a connector call is unsuccessful" should {
      "return not found error for no matching id" in new Test {
        MockRetrieveBusinessDetailsConnector.retrieveBusinessDetails(badRequestData)
          .returns(Future.successful(Right(ResponseWrapper("resultId", desMultiResponseBody))))

        await(service.retrieveBusinessDetailsService(badRequestData)) shouldBe Left(ErrorWrapper(Some("resultId"), NoBusinessFoundError))
    }
    "a connector call is unsuccessful" should {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"return ${error.code} when $desErrorCode error is returned from the service" in new Test {

            MockRetrieveBusinessDetailsConnector.retrieveBusinessDetails(requestData)
              .returns(Future.successful(Left(ResponseWrapper("resultId", DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieveBusinessDetailsService(requestData)) shouldBe Left(ErrorWrapper(Some("resultId"), error))
          }

        val input = Seq(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_MTDBSA", DownstreamError),
          ("NOT_FOUND_NINO", NotFoundError),
          ("NOT_FOUND_MTDBSA", DownstreamError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError),
        )
        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
