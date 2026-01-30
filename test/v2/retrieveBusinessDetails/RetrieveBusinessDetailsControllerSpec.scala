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

package v2.retrieveBusinessDetails

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.*
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Result
import routing.Version2
import utils.MockIdGenerator
import v2.retrieveBusinessDetails.model.request.RetrieveBusinessDetailsRequestData
import v2.retrieveBusinessDetails.model.response.downstream.{LatencyDetails, LatencyIndicator, QuarterReportingType, QuarterTypeElection}
import v2.retrieveBusinessDetails.model.response.{AccountingPeriod, RetrieveBusinessDetailsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveBusinessDetailsControllerSpec
    extends ControllerBaseSpec(Version2)
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveBusinessDetailsService
    with MockRetrieveBusinessDetailsValidatorFactory
    with MockIdGenerator
    with MockAppConfig {

  private val businessId = "XAIS12345678910"

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
      |   "quarterlyTypeChoice": {
      |     "quarterlyPeriodType": "standard",
      |     "taxYearOfChoice": "2023-24"
      |   }
      |}
        """.stripMargin
  )

  private val responseData = RetrieveBusinessDetailsResponse(
    businessId = "XAIS12345678910",
    typeOfBusiness = TypeOfBusiness.`self-employment`,
    tradingName = Some("Aardvark Window Cleaning Services"),
    accountingPeriods = Some(Seq(AccountingPeriod("2018-04-06", "2019-04-05"))),
    commencementDate = Some("2016-09-24"),
    cessationDate = Some("2020-03-24"),
    businessAddressLineOne = Some("6 Harpic Drive"),
    businessAddressLineTwo = Some("Domestos Wood"),
    businessAddressLineThree = Some("ToiletDucktown"),
    businessAddressLineFour = Some("CIFSHIRE"),
    businessAddressPostcode = Some("SW4F 3GA"),
    businessAddressCountryCode = Some("GB"),
    firstAccountingPeriodStartDate = Some("2018-04-06"),
    firstAccountingPeriodEndDate = Some("2018-12-12"),
    latencyDetails = Some(
      LatencyDetails(
        "2018-12-12",
        TaxYear.fromDownstream("2018"),
        LatencyIndicator.Annual,
        TaxYear.fromDownstream("2019"),
        LatencyIndicator.Quarterly)),
    yearOfMigration = Some("2023"),
    quarterlyTypeChoice = Some(QuarterTypeElection(QuarterReportingType.STANDARD, TaxYear.fromMtd("2023-24")))
  )

  private val requestData = RetrieveBusinessDetailsRequestData(Nino(nino), BusinessId(businessId))

  "handleRequest" should {
    "return successful response with status OK" when {
      "valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveBusinessDetailsService
          .retrieveBusinessDetailsService(requestData)
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

        MockRetrieveBusinessDetailsService
          .retrieveBusinessDetailsService(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller: RetrieveBusinessDetailsController = new RetrieveBusinessDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveBusinessDetailsValidatorFactory,
      service = mockRetrieveBusinessDetailsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId)(fakeGetRequest)
  }

}
