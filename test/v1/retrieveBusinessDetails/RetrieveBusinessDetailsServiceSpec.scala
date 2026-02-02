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

package v1.retrieveBusinessDetails

import api.models.domain.*
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import config.MockFeatureSwitches
import v1.retrieveBusinessDetails.model.request.RetrieveBusinessDetailsRequestData
import v1.retrieveBusinessDetails.model.response.downstream.*
import v1.retrieveBusinessDetails.model.response.{AccountingPeriod, RetrieveBusinessDetailsResponse}

import scala.concurrent.Future

class RetrieveBusinessDetailsServiceSpec extends ServiceSpec {

  private val nino = Nino("AA123456A")

  private def requestDataFor(businessId: String) = RetrieveBusinessDetailsRequestData(nino, BusinessId(businessId))

  private val yearOfMigration = Some("migrationYear")

  private def propertyData(incomeSourceId: String) =
    PropertyData(
      incomeSourceType = Some(TypeOfBusiness.`foreign-property`),
      incomeSourceId = incomeSourceId,
      accountingPeriodStartDate = "accStartDate",
      accountingPeriodEndDate = "accEndDate",
      firstAccountingPeriodStartDate = None,
      firstAccountingPeriodEndDate = None,
      latencyDetails = None,
      tradingStartDate = None,
      cessationDate = None,
      quarterTypeElection = Some(QuarterTypeElection(QuarterReportingType.`CALENDAR`, TaxYear.fromMtd("2023-24")))
    )

  private def propertyResponse(incomeSourceId: String) = RetrieveBusinessDetailsResponse(
    businessId = incomeSourceId,
    typeOfBusiness = TypeOfBusiness.`foreign-property`,
    tradingName = None,
    accountingPeriods = List(AccountingPeriod("accStartDate", "accEndDate")),
    commencementDate = None,
    cessationDate = None,
    businessAddressLineOne = None,
    businessAddressLineTwo = None,
    businessAddressLineThree = None,
    businessAddressLineFour = None,
    businessAddressPostcode = None,
    businessAddressCountryCode = None,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    latencyDetails = None,
    yearOfMigration = yearOfMigration,
    quarterlyTypeChoice = Some(QuarterTypeElection(QuarterReportingType.`CALENDAR`, TaxYear.fromMtd("2023-24")))
  )

  private def businessData(incomeSourceId: String) =
    BusinessData(
      incomeSourceId = incomeSourceId,
      accountingPeriodStartDate = "accStartDate",
      accountingPeriodEndDate = "accEndDate",
      tradingName = None,
      businessAddressDetails = None,
      firstAccountingPeriodStartDate = None,
      firstAccountingPeriodEndDate = None,
      latencyDetails = None,
      tradingStartDate = None,
      cessationDate = None,
      quarterTypeElection = Some(QuarterTypeElection(QuarterReportingType.`CALENDAR`, TaxYear.fromMtd("2023-24")))
    )

  private def selfEmploymentResponse(incomeSourceId: String) = RetrieveBusinessDetailsResponse(
    businessId = incomeSourceId,
    typeOfBusiness = TypeOfBusiness.`self-employment`,
    tradingName = None,
    accountingPeriods = List(AccountingPeriod("accStartDate", "accEndDate")),
    commencementDate = None,
    cessationDate = None,
    businessAddressLineOne = None,
    businessAddressLineTwo = None,
    businessAddressLineThree = None,
    businessAddressLineFour = None,
    businessAddressPostcode = None,
    businessAddressCountryCode = None,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    latencyDetails = None,
    yearOfMigration = yearOfMigration,
    quarterlyTypeChoice = Some(QuarterTypeElection(QuarterReportingType.`CALENDAR`, TaxYear.fromMtd("2023-24")))
  )

  "service" when {
    "a connector call is successful" when {
      "a unique matching property business is found" must {
        "find and convert to MTD" in new Test with scp005aEnabled {
          testServiceWith(
            requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(yearOfMigration, businessData = None, propertyData = Some(List(propertyData("businessId"))))
          ) shouldBe Right(ResponseWrapper(correlationId, propertyResponse("businessId")))
        }
      }

      "the scp005a_quarterlyTypeChoice feature switch is disabled" must {
        "return a response with the quarterlyTypeChoice field removed" in new Test with scp005aDisabled {
          testServiceWith(
            requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(
              yearOfMigration,
              businessData = None,
              propertyData = Some(List(propertyData("otherBusinessId"), propertyData("businessId"))))
          ) shouldBe Right(ResponseWrapper(correlationId, propertyResponse("businessId").copy(quarterlyTypeChoice = None)))
        }
      }

      "a unique matching self-employment business is found" must {
        "find and convert to MTD" in new Test with scp005aEnabled {
          testServiceWith(
            requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(
              yearOfMigration,
              businessData = Some(List(businessData("otherBusinessId"), businessData("businessId"))),
              propertyData = None)
          ) shouldBe Right(ResponseWrapper(correlationId, selfEmploymentResponse("businessId")))
        }
      }

      "multiple matching property businesses are found" must {
        "return duplicate result" in new Test with scp005aEnabled {
          testServiceWith(
            requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(
              yearOfMigration,
              businessData = None,
              propertyData = Some(List(propertyData("businessId"), propertyData("businessId"))))
          ) shouldBe Left(ErrorWrapper(correlationId, InternalError))
        }
      }

      "multiple matching self-employment businesses are found" must {
        "return an internal error" in new Test with scp005aEnabled {
          testServiceWith(
            requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(
              yearOfMigration,
              businessData = Some(List(businessData("businessId"), businessData("businessId"))),
              propertyData = None)
          ) shouldBe Left(ErrorWrapper(correlationId, InternalError))
        }
      }

      "a matching property business and a self-employment business are found" must {
        "return duplicate result" in new Test with scp005aEnabled {
          testServiceWith(
            requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(
              yearOfMigration,
              businessData = Some(List(businessData("businessId"))),
              propertyData = Some(List(propertyData("businessId"))))
          ) shouldBe Left(ErrorWrapper(correlationId, InternalError))
        }
      }

      "nothing matching is found" must {
        "return a not found result" in new Test with scp005aEnabled {
          testServiceWith(
            requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(yearOfMigration, businessData = None, propertyData = None)) shouldBe Left(
            ErrorWrapper(correlationId, NoBusinessFoundError))
        }
      }

      "nothing found when the property/business data arrays are present but empty" must {
        "return a not found result" in new Test with scp005aEnabled {
          testServiceWith(
            requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(yearOfMigration, businessData = Some(Nil), propertyData = Some(Nil))) shouldBe Left(
            ErrorWrapper(correlationId, NoBusinessFoundError))
        }
      }
    }

    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test with scp005aEnabled {
          val requestData: RetrieveBusinessDetailsRequestData = requestDataFor("someBusinessId")

          MockedRetrieveBusinessDetailsConnector
            .retrieveBusinessDetails(nino)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[RetrieveBusinessDetailsResponse] = await(service.retrieveBusinessDetailsService(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("INVALID_NINO", NinoFormatError),
        ("INVALID_MTDBSA", InternalError),
        ("UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError),
        ("NOT_FOUND_NINO", NotFoundError),
        ("NOT_FOUND_MTDBSA", InternalError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      val extraIfsErrors = List(
        ("INVALID_MTD_ID", InternalError),
        ("INVALID_CORRELATIONID", InternalError),
        ("INVALID_IDTYPE", InternalError),
        ("NOT_FOUND", NotFoundError)
      )

      val hipErrors = List(
        ("001", InternalError),
        ("006", NotFoundError),
        ("007", InternalError),
        ("008", NoBusinessFoundError)
      )

      (errors ++ extraIfsErrors ++ hipErrors).foreach(serviceError.tupled)
    }
  }

  private trait Test extends MockRetrieveBusinessDetailsConnector with MockFeatureSwitches {
    MockedFeatureSwitches.isIfsEnabled.returns(true).anyNumberOfTimes()

    protected val service = new RetrieveBusinessDetailsService(mockRetrieveBusinessDetailsConnector)

    protected def testServiceWith(requestData: RetrieveBusinessDetailsRequestData,
                                  downstreamResponse: RetrieveBusinessDetailsDownstreamResponse): ServiceOutcome[RetrieveBusinessDetailsResponse] = {
      MockedRetrieveBusinessDetailsConnector
        .retrieveBusinessDetails(nino) returns Future.successful(Right(ResponseWrapper(correlationId, downstreamResponse)))

      await(service.retrieveBusinessDetailsService(requestData))
    }

  }

  private trait scp005aEnabled extends MockFeatureSwitches {
    MockedFeatureSwitches.isScp005aQuarterlyTypeChoiceEnabled.returns(true).anyNumberOfTimes()
  }

  private trait scp005aDisabled extends MockFeatureSwitches {
    MockedFeatureSwitches.isScp005aQuarterlyTypeChoiceEnabled.returns(false).anyNumberOfTimes()
  }

}
