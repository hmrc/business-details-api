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

package v2.retrieveAccountingType

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.{ServiceOutcome, ServiceSpec}
import v2.common.models.AccountingType
import v2.retrieveAccountingType.model.request.*
import v2.retrieveAccountingType.model.response.RetrieveAccountingTypeResponse

import scala.concurrent.Future

class RetrieveAccountingTypeServiceSpec extends ServiceSpec {

  private val nino             = Nino("AA123456A")
  private val businessId       = BusinessId("XAIS12345678910")
  private val taxYear          = TaxYear.fromMtd("2024-25")
  private val requestData      = RetrieveAccountingTypeRequest(nino, businessId, taxYear)
  private val expectedResponse = RetrieveAccountingTypeResponse(AccountingType.CASH)

  "service" when {
    "a connector call is successful" should {
      "return a mapped result" in new Test {
        MockedRetrieveAccountingTypeConnector
          .retrieveAccountingType(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, expectedResponse))))

        val result: ServiceOutcome[RetrieveAccountingTypeResponse] = await(service.retrieveAccountingType(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, expectedResponse))
      }
    }
    "a connector call is unsuccessful" should {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"return ${error.code} when $downstreamErrorCode error is returned from the service" in new Test {
          MockedRetrieveAccountingTypeConnector
            .retrieveAccountingType(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[RetrieveAccountingTypeResponse] = await(service.retrieveAccountingType(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("1215", NinoFormatError),
        ("1117", TaxYearFormatError),
        ("1007", BusinessIdFormatError),
        ("1122", InternalError),
        ("1229", InternalError),
        ("5009", InternalError),
        ("5010", NotFoundError),
        ("UNMATCHED_STUB_ERROR", RuleIncorrectGovTestScenarioError)
      )

      errors.foreach(serviceError.tupled)
    }
  }

  private trait Test extends MockRetrieveAccountingTypeConnector {

    protected val service = new RetrieveAccountingTypeService(mockRetrieveAccountingTypeConnector)

  }

}
