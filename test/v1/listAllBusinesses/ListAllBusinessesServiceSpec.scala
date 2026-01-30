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

package v1.listAllBusinesses

import api.models.domain.{Nino, TypeOfBusiness}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.listAllBusinesses.model.request.ListAllBusinessesRequestData
import v1.listAllBusinesses.model.response.{Business, ListAllBusinessesResponse}
import v1.retrieveBusinessDetails.MockRetrieveBusinessDetailsConnector
import v1.retrieveBusinessDetails.model.response.downstream.{BusinessData, RetrieveBusinessDetailsDownstreamResponse}

import scala.concurrent.Future

class ListAllBusinessesServiceSpec extends ServiceSpec {

  private val nino        = Nino("AA123456A")
  private val requestData = ListAllBusinessesRequestData(nino)

  "service" when {
    "a connector call is successful" should {
      "return a converted result" in new Test {
        private val downstreamResponse = RetrieveBusinessDetailsDownstreamResponse(
          yearOfMigration = None,
          businessData = Some(
            Seq(BusinessData(
              incomeSourceId = "someBusinessId",
              accountingPeriodStartDate = "ignoredStartDate",
              accountingPeriodEndDate = "ignoredEndDate",
              tradingName = None,
              businessAddressDetails = None,
              firstAccountingPeriodStartDate = None,
              firstAccountingPeriodEndDate = None,
              latencyDetails = None,
              tradingStartDate = None,
              cessationDate = None,
              quarterTypeElection = None
            ))),
          propertyData = None
        )

        MockedRetrieveBusinessDetailsConnector.retrieveBusinessDetails(nino) returns
          Future.successful(Right(ResponseWrapper(correlationId, downstreamResponse)))

        await(service.listAllBusinessesService(requestData)) shouldBe
          Right(
            ResponseWrapper(
              correlationId,
              ListAllBusinessesResponse(List(Business(TypeOfBusiness.`self-employment`, "someBusinessId", None)))
            ))
      }
    }

    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test {

          MockedRetrieveBusinessDetailsConnector.retrieveBusinessDetails(nino) returns
            Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode)))))

          await(service.listAllBusinessesService(requestData)) shouldBe
            Left(ErrorWrapper(correlationId, error))
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
        ("008", InternalError)
      )

      (errors ++ extraIfsErrors ++ hipErrors).foreach(serviceError.tupled)
    }
  }

  private trait Test extends MockRetrieveBusinessDetailsConnector {
    protected val service = new ListAllBusinessesService(mockRetrieveBusinessDetailsConnector)
  }

}
