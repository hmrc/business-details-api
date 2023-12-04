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

import api.models.domain.{AccountingType, BusinessId, Nino, TypeOfBusiness}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import config.MockFeatureSwitches
import v1.connectors.MockRetrieveBusinessDetailsConnector
import v1.models.request.retrieveBusinessDetails.RetrieveBusinessDetailsRequestData
import v1.models.response.retrieveBusinessDetails.downstream.{BusinessData, PropertyData, RetrieveBusinessDetailsDownstreamResponse}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}

import scala.concurrent.Future

class RetrieveBusinessDetailsServiceSpec extends ServiceSpec {

  private val nino = Nino("AA123456A")

  private def requestDataFor(businessId: String) = RetrieveBusinessDetailsRequestData(nino, BusinessId(businessId))

  private val cashOrAcruals = Some(AccountingType.ACCRUALS)
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
      cashOrAccruals = cashOrAcruals,
      tradingStartDate = None,
      cessationDate = None
    )

  private def propertyResponse(incomeSourceId: String) = RetrieveBusinessDetailsResponse(
    businessId = incomeSourceId,
    typeOfBusiness = TypeOfBusiness.`foreign-property`,
    tradingName = None,
    accountingPeriods = Seq(AccountingPeriod("accStartDate", "accEndDate")),
    accountingType = cashOrAcruals,
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
    yearOfMigration = yearOfMigration
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
      cashOrAccruals = cashOrAcruals,
      tradingStartDate = None,
      cessationDate = None
    )


  private def selfEmploymentResponse(incomeSourceId: String) = RetrieveBusinessDetailsResponse(
    businessId = incomeSourceId,
    typeOfBusiness = TypeOfBusiness.`self-employment`,
    tradingName = None,
    accountingPeriods = Seq(AccountingPeriod("accStartDate", "accEndDate")),
    accountingType = cashOrAcruals,
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
    yearOfMigration = yearOfMigration
  )


  "service" when {
    "a connector call is successful" when {
      "a unique matching property business is found" must {
        s"find and convert to MTD" in new Test {
          testServiceWith(requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(yearOfMigration,
              businessData = None,
              propertyData = Some(Seq(propertyData("otherBusinessId"), propertyData("businessId")))
            )) shouldBe Right(ResponseWrapper(correlationId, propertyResponse("businessId")))
        }
      }

      "a unique matching self-employment business is found" must {
        "find and convert to MTD" in new Test {
          testServiceWith(requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(yearOfMigration,
              businessData = Some(Seq(businessData("otherBusinessId"), businessData("businessId"))),
              propertyData = None
            )) shouldBe Right(ResponseWrapper(correlationId, selfEmploymentResponse("businessId")))
        }
      }

      "multiple matching property businesses are found" must {
        "return duplicate result" in new Test {
          testServiceWith(requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(yearOfMigration,
              businessData = None,
              propertyData = Some(Seq(propertyData("businessId"), propertyData("businessId")))
            )) shouldBe Left(ErrorWrapper(correlationId, InternalError))
        }
      }

      "multiple matching self-employment businesses are found" must {
        "return an internal error" in new Test {
          testServiceWith(requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(yearOfMigration,
              businessData = Some(Seq(businessData("businessId"), businessData("businessId"))),
              propertyData = None
            )) shouldBe Left(ErrorWrapper(correlationId, InternalError))
        }
      }

      "a matching property business and a self-employment business are found" must {
        "return duplicate result" in new Test {
          testServiceWith(requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(yearOfMigration,
              businessData = Some(Seq(businessData("businessId"))),
              propertyData = Some(Seq(propertyData("businessId")))
            )) shouldBe Left(ErrorWrapper(correlationId, InternalError))
        }
      }

      "nothing matching is found" must {
        "return a not found result" in new Test {
          testServiceWith(requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(yearOfMigration,
              businessData = None,
              propertyData = None
            )) shouldBe Left(ErrorWrapper(correlationId, NoBusinessFoundError))
        }
      }

      "nothing found when the property/business data arrays are present but empty" must {
        "return a not found result" in new Test {
          testServiceWith(requestDataFor("businessId"),
            RetrieveBusinessDetailsDownstreamResponse(yearOfMigration,
              businessData = Some(Nil),
              propertyData = Some(Nil)
            )) shouldBe Left(ErrorWrapper(correlationId, NoBusinessFoundError))
        }
      }
    }

    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test {
          val requestData = requestDataFor("someBusinessId")

          MockedRetrieveBusinessDetailsConnector
            .retrieveBusinessDetails(requestData)
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

      (errors ++ extraIfsErrors).foreach((serviceError _).tupled)
    }
  }

  private trait Test extends MockRetrieveBusinessDetailsConnector with MockFeatureSwitches {
    MockFeatureSwitches.isIfsEnabled.returns(true).anyNumberOfTimes()

    protected val service = new RetrieveBusinessDetailsService(mockRetrieveBusinessDetailsConnector)

    protected def testServiceWith(requestData: RetrieveBusinessDetailsRequestData, downstreamResponse: RetrieveBusinessDetailsDownstreamResponse): ServiceOutcome[RetrieveBusinessDetailsResponse] = {
      MockedRetrieveBusinessDetailsConnector
        .retrieveBusinessDetails(requestData) returns Future.successful(Right(ResponseWrapper(correlationId, downstreamResponse)))

      await(service.retrieveBusinessDetailsService(requestData))
    }
  }
}
