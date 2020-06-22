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

package v1.support

import support.UnitSpec
import utils.Logging
import v1.controllers.EndpointLogContext
import v1.models.domain.TypeOfBusiness
import v1.models.domain.accountingType.AccountingType
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.retrieveBusinessDetails.des.{BusinessDetails, RetrieveBusinessDetailsDesResponse}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}

class DesResponseMappingSupportSpec extends UnitSpec {

  implicit val logContext: EndpointLogContext = EndpointLogContext("ctrl", "ep")
  val mapping: DesResponseMappingSupport with Logging = new DesResponseMappingSupport with Logging {}

  val correlationId = "someCorrelationId"

  object Error1 extends MtdError("msg", "code1")

  object Error2 extends MtdError("msg", "code2")

  object ErrorBvrMain extends MtdError("msg", "bvrMain")

  object ErrorBvr extends MtdError("msg", "bvr")

  val errorCodeMap: PartialFunction[String, MtdError] = {
    case "ERR1" => Error1
    case "ERR2" => Error2
    case "DS" => DownstreamError
  }

  "mapping Des errors" when {
    "single error" when {
      "the error code is in the map provided" must {
        "use the mapping and wrap" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("ERR1")))) shouldBe
            ErrorWrapper(Some(correlationId), Error1)
        }
      }

      "the error code is not in the map provided" must {
        "default to DownstreamError and wrap" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode("UNKNOWN")))) shouldBe
            ErrorWrapper(Some(correlationId), DownstreamError)
        }
      }
    }

    "multiple errors" when {
      "the error codes is in the map provided" must {
        "use the mapping and wrap with main error type of BadRequest" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("ERR1"), DesErrorCode("ERR2"))))) shouldBe
            ErrorWrapper(Some(correlationId), BadRequestError, Some(Seq(Error1, Error2)))
        }
      }

      "the error code is not in the map provided" must {
        "default main error to DownstreamError ignore other errors" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("ERR1"), DesErrorCode("UNKNOWN"))))) shouldBe
            ErrorWrapper(Some(correlationId), DownstreamError)
        }
      }

      "one of the mapped errors is DownstreamError" must {
        "wrap the errors with main error type of DownstreamError" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode("ERR1"), DesErrorCode("DS"))))) shouldBe
            ErrorWrapper(Some(correlationId), DownstreamError)
        }
      }
    }

    "the error code is an OutboundError" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain))) shouldBe
          ErrorWrapper(Some(correlationId), ErrorBvrMain)
      }
    }

    "the error code is an OutboundError with multiple errors" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain, Some(Seq(ErrorBvr))))) shouldBe
          ErrorWrapper(Some(correlationId), ErrorBvrMain, Some(Seq(ErrorBvr)))
      }
    }
  }
  "filterId" should {
    val desSingleBusiness: RetrieveBusinessDetailsDesResponse = RetrieveBusinessDetailsDesResponse(Seq(BusinessDetails(
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
    val desMultipleBusinessInSeq = RetrieveBusinessDetailsDesResponse(Seq(BusinessDetails(
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
      )))

    val DesSingleBusinessDetailsRepeated = RetrieveBusinessDetailsDesResponse(Seq(BusinessDetails(
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
      )))

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
          Left(ErrorWrapper(Some(""), NoBusinessFoundError))
      }
    }
    "return downstream error" when {
      "multiple businesses are passed with multiple having the correct id" in {
        mapping.filterId(ResponseWrapper("", DesSingleBusinessDetailsRepeated), "XAIS12345678910") shouldBe
          Left(ErrorWrapper(Some(""), DownstreamError))
      }
    }
  }
}
