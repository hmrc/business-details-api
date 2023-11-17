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
import api.models.domain.{AccountingType, BusinessId, Nino, TypeOfBusiness}
import api.models.errors.{
  DownstreamErrorCode,
  DownstreamErrors,
  ErrorWrapper,
  InternalError,
  MtdError,
  NinoFormatError,
  NoBusinessFoundError,
  NotFoundError,
  RuleIncorrectGovTestScenarioError
}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import config.MockAppConfig
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.MockRetrieveBusinessDetailsConnector
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRequestData
import v1.models.response.retrieveBusinessDetails.downstream.{
  BusinessDetails,
  LatencyDetails,
  LatencyIndicator,
  RetrieveBusinessDetailsDownstreamResponse
}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}

import scala.concurrent.Future

class RetrieveBusinessDetailsServiceSpec extends ServiceSpec {

  private val validNino       = Nino("AA123456A")
  private val validBusinessId = BusinessId("XAIS12345678910")
  private val requestData     = RetrieveBusinessDetailsRequestData(validNino, validBusinessId)
  private val badRequestData  = RetrieveBusinessDetailsRequestData(validNino, BusinessId("SDFG3456782190"))

  private val responseBody = RetrieveBusinessDetailsResponse(
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

  private val responseBodyWithoutAdditionalFields = RetrieveBusinessDetailsResponse(
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
    None,
    None,
    None,
    None
  )

  private val downstreamSingleResponseBody = RetrieveBusinessDetailsDownstreamResponse(
    Seq(
      BusinessDetails(
        "XAIS12345678910",
        TypeOfBusiness.`self-employment`,
        Some("Aardvark Window Cleaning Services"),
        Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
        Some("2018-04-06"),
        Some("2018-12-12"),
        Some(LatencyDetails("2018-12-12", "2018", LatencyIndicator.Annual, "2019", LatencyIndicator.Quarterly)),
        Some("2023"),
        Some(AccountingType.ACCRUALS),
        Some("2016-09-24"),
        Some("2020-03-24"),
        Some("6 Harpic Drive"),
        Some("Domestos Wood"),
        Some("ToiletDucktown"),
        Some("CIFSHIRE"),
        Some("SW4F 3GA"),
        Some("GB")
      )))

  private val downstreamWithoutAdditionalFieldsSingleResponseBody = RetrieveBusinessDetailsDownstreamResponse(
    Seq(
      BusinessDetails(
        "XAIS12345678910",
        TypeOfBusiness.`self-employment`,
        Some("Aardvark Window Cleaning Services"),
        Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
        Some("2018-04-06"),
        Some("2018-12-12"),
        Some(LatencyDetails("2018-12-12", "2017-18", LatencyIndicator.Annual, "2018-19", LatencyIndicator.Quarterly)),
        Some("2023"),
        Some(AccountingType.ACCRUALS),
        Some("2016-09-24"),
        Some("2020-03-24"),
        Some("6 Harpic Drive"),
        Some("Domestos Wood"),
        Some("ToiletDucktown"),
        Some("CIFSHIRE"),
        Some("SW4F 3GA"),
        Some("GB")
      )))

  private val downstreamSingleWithoutAdditionalFieldsResponseBody = RetrieveBusinessDetailsDownstreamResponse(
    Seq(
      BusinessDetails(
        "XAIS12345678910",
        TypeOfBusiness.`self-employment`,
        Some("Aardvark Window Cleaning Services"),
        Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
        None,
        None,
        None,
        None,
        Some(AccountingType.ACCRUALS),
        Some("2016-09-24"),
        Some("2020-03-24"),
        Some("6 Harpic Drive"),
        Some("Domestos Wood"),
        Some("ToiletDucktown"),
        Some("CIFSHIRE"),
        Some("SW4F 3GA"),
        Some("GB")
      )))

  private val downstreamMultiResponseBody = RetrieveBusinessDetailsDownstreamResponse(
    Seq(
      BusinessDetails(
        "XAIS12345678910",
        TypeOfBusiness.`self-employment`,
        Some("Aardvark Window Cleaning Services"),
        Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
        Some("2018-04-06"),
        Some("2018-12-12"),
        Some(LatencyDetails("2018-12-12", "2018", LatencyIndicator.Annual, "2019", LatencyIndicator.Quarterly)),
        Some("2023"),
        Some(AccountingType.ACCRUALS),
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
        Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
        Some("2018-04-06"),
        Some("2018-12-12"),
        Some(LatencyDetails("2018-12-12", "2018", LatencyIndicator.Annual, "2019", LatencyIndicator.Quarterly)),
        Some("2023"),
        Some(AccountingType.ACCRUALS),
        Some("2016-09-24"),
        Some("2020-03-24"),
        Some("6 Harpic Drive"),
        Some("Domestos Wood"),
        Some("ToiletDucktown"),
        Some("CIFSHIRE"),
        Some("SW4F 3GA"),
        Some("GB")
      )
    ))

  trait Test extends MockRetrieveBusinessDetailsConnector with MockAppConfig {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    val isEnabled: Boolean                      = true

    MockedAppConfig.featureSwitches
      .returns(Configuration("retrieveAdditionalFields.enabled" -> isEnabled))
      .anyNumberOfTimes()

    val service = new RetrieveBusinessDetailsService(mockRetrieveBusinessDetailsConnector, mockAppConfig)

  }

  "service" when {
    "a connector call is successful" should {
      "return a mapped result from a single downstream response" in new Test {
        MockedRetrieveBusinessDetailsConnector
          .retrieveBusinessDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamSingleResponseBody))))

        await(service.retrieveBusinessDetailsService(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseBody))
      }

      "return a mapped result from a downstream response when retrieveAdditionalFields is disabled" in new Test {
        override val isEnabled: Boolean = false
        MockedRetrieveBusinessDetailsConnector
          .retrieveBusinessDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamWithoutAdditionalFieldsSingleResponseBody))))

        await(service.retrieveBusinessDetailsService(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseBody))
      }

      "return a mapped result from multiple downstream responses" in new Test {
        MockedRetrieveBusinessDetailsConnector
          .retrieveBusinessDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamMultiResponseBody))))

        await(service.retrieveBusinessDetailsService(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseBody))
      }
      "return a mapped result when additional fields are not present" in new Test {
        MockedRetrieveBusinessDetailsConnector
          .retrieveBusinessDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamSingleWithoutAdditionalFieldsResponseBody))))

        await(service.retrieveBusinessDetailsService(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseBodyWithoutAdditionalFields))
      }
    }
    "a connector call is unsuccessful" should {
      "return not found error for no matching id" in new Test {
        MockedRetrieveBusinessDetailsConnector
          .retrieveBusinessDetails(badRequestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamMultiResponseBody))))

        await(service.retrieveBusinessDetailsService(badRequestData)) shouldBe Left(ErrorWrapper(correlationId, NoBusinessFoundError))
      }
      "a connector call is unsuccessful" should {
        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"return ${error.code} when $desErrorCode error is returned from the service" in new Test {
            MockedRetrieveBusinessDetailsConnector
              .retrieveBusinessDetails(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.retrieveBusinessDetailsService(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
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

}
