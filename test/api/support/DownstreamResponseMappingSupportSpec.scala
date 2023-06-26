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

package api.support

import api.controllers.EndpointLogContext
import api.models.domain.TypeOfBusiness
import api.models.domain.accountingType.AccountingType
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.http.Status.BAD_REQUEST
import support.UnitSpec
import utils.Logging
import v1.models.response.retrieveBusinessDetails.des.{BusinessDetails, RetrieveBusinessDetailsDesResponse}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}

class DownstreamResponseMappingSupportSpec extends UnitSpec {

  implicit val logContext: EndpointLogContext = EndpointLogContext("ctrl", "ep")

  val mapping: DownstreamResponseMappingSupport with Logging = new DownstreamResponseMappingSupport with Logging {}

  val correlationId = "someCorrelationId"
  val errorCodeMap: PartialFunction[String, MtdError] = {
    case "ERR1" => Error1
    case "ERR2" => Error2
    case "DS"   => InternalError
  }

  object Error1 extends MtdError("msg", "code1", BAD_REQUEST)

  object Error2 extends MtdError("msg", "code2", BAD_REQUEST)

  object ErrorBvrMain extends MtdError("msg", "bvrMain", BAD_REQUEST)

  object ErrorBvr extends MtdError("msg", "bvr", BAD_REQUEST)

  "mapping Des errors" when {
    "single error" when {
      "the error code is in the map provided" must {
        "use the mapping and wrap" in {
          mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("ERR1")))) shouldBe
            ErrorWrapper(correlationId, Error1)
        }
      }

      "the error code is not in the map provided" must {
        "default to DownstreamError and wrap" in {
          mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("UNKNOWN")))) shouldBe
            ErrorWrapper(correlationId, InternalError)
        }
      }
    }

    "multiple errors" when {
      "the error codes is in the map provided" must {
        "use the mapping and wrap with main error type of BadRequest" in {
          mapping.mapDownstreamErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("ERR2"))))) shouldBe
            ErrorWrapper(correlationId, BadRequestError, Some(Seq(Error1, Error2)))
        }
      }

      "the error code is not in the map provided" must {
        "default main error to DownstreamError ignore other errors" in {
          mapping.mapDownstreamErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("UNKNOWN"))))) shouldBe
            ErrorWrapper(correlationId, InternalError)
        }
      }

      "one of the mapped errors is DownstreamError" must {
        "wrap the errors with main error type of DownstreamError" in {
          mapping.mapDownstreamErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("DS"))))) shouldBe
            ErrorWrapper(correlationId, InternalError)
        }
      }
    }

    "the error code is an OutboundError" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain))) shouldBe
          ErrorWrapper(correlationId, ErrorBvrMain)
      }
    }

    "the error code is an OutboundError with multiple errors" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain, Some(Seq(ErrorBvr))))) shouldBe
          ErrorWrapper(correlationId, ErrorBvrMain, Some(Seq(ErrorBvr)))
      }
    }
  }

  "filterId" should {
    val desSingleBusiness: RetrieveBusinessDetailsDesResponse = RetrieveBusinessDetailsDesResponse(
      Seq(BusinessDetails(
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
        Some("GB")
      )))
    val desMultipleBusinessInSeq = RetrieveBusinessDetailsDesResponse(
      Seq(
        BusinessDetails(
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
          Some("GB")
        ),
        BusinessDetails(
          "XAIS0987654321",
          TypeOfBusiness.`self-employment`,
          Some("Aardvark Window Cleaning Services"),
          Seq(AccountingPeriod("2018-04-06", "2019-04-05")),
          Some(AccountingType.ACCRUALS),
          Some("2016-09-24"),
          Some("2020-03-24"),
          Some("6 Test Drive"),
          Some("Test Wood"),
          Some("Test Town"),
          Some("TESTSHIRE"),
          Some("TE4 3ST"),
          Some("FR")
        )
      ))

    val DesSingleBusinessDetailsRepeated = RetrieveBusinessDetailsDesResponse(
      Seq(
        BusinessDetails(
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
          Some("GB")
        ),
        BusinessDetails(
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
          Some("GB")
        )
      ))

    val responseBusiness = RetrieveBusinessDetailsResponse(
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
      Some("GB")
    )

    "return a single businesses details" when {
      "a single business is passed in with correct id" in {
        mapping.filterId(ResponseWrapper("", desSingleBusiness), "XAIS12345678910") shouldBe Right(ResponseWrapper("", responseBusiness))
      }

      "multiple businesses are passed with one correct id" in {
        mapping.filterId(ResponseWrapper("", desMultipleBusinessInSeq), "XAIS12345678910") shouldBe Right(ResponseWrapper("", responseBusiness))
      }
    }
    "return no business details error" when {
      "businesses are passed with none having the correct id" in {
        mapping.filterId(ResponseWrapper("", desMultipleBusinessInSeq), "XAIS6789012345") shouldBe
          Left(ErrorWrapper("", NoBusinessFoundError))
      }
    }
    "return downstream error" when {
      "multiple businesses are passed with multiple having the correct id" in {
        mapping.filterId(ResponseWrapper("", DesSingleBusinessDetailsRepeated), "XAIS12345678910") shouldBe
          Left(ErrorWrapper("", InternalError))
      }
    }
  }

}
