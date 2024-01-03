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

package api.models.errors

import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json
import support.UnitSpec

class ErrorWrapperSpec extends UnitSpec {

  val correlationId = "X-123"

  "Rendering a error response with one error" should {
    val error = ErrorWrapper(correlationId, NinoFormatError)

    val json = Json.parse(
      """
        |{
        |   "code": "FORMAT_NINO",
        |   "message": "The provided NINO is invalid"
        |}
      """.stripMargin
    )

    "generate the correct JSON" in {
      Json.toJson(error) shouldBe json
    }
  }

  "Rendering a error response with one error and an empty sequence of errors" should {
    val error = ErrorWrapper(correlationId, NinoFormatError)

    val json = Json.parse(
      """
        |{
        |   "code": "FORMAT_NINO",
        |   "message": "The provided NINO is invalid"
        |}
      """.stripMargin
    )

    "generate the correct JSON" in {
      Json.toJson(error) shouldBe json
    }
  }

  "Rendering a error response with two errors" should {
    val error = ErrorWrapper(
      correlationId,
      BadRequestError,
      Some(
        Seq(
          NinoFormatError,
          TaxYearFormatError
        )))

    val json = Json.parse(
      """
        |{
        |   "code": "INVALID_REQUEST",
        |   "message": "Invalid request",
        |   "errors": [
        |       {
        |         "code": "FORMAT_NINO",
        |         "message": "The provided NINO is invalid"
        |       },
        |       {
        |         "code": "FORMAT_TAX_YEAR",
        |         "message": "The provided tax year is invalid"
        |       }
        |   ]
        |}
      """.stripMargin
    )

    "generate the correct JSON" in {
      Json.toJson(error) shouldBe json
    }
  }

  "containsAnyOf" should {
    "return true when the error matches one of the errorsToCheck" in {
      val error1 = MtdError("ERROR_1", "Error message 1", BAD_REQUEST)
      val error2 = MtdError("ERROR_2", "Error message 2", BAD_REQUEST)

      val errorWrapper = ErrorWrapper("correlationId", error1)

      val result = errorWrapper.containsAnyOf(error1, error2)
      result shouldBe true
    }

    "return false when the error does not match one of the errorsToCheck" in {
      val error1 = MtdError("ERROR_1", "Error message 1", BAD_REQUEST)
      val error2 = MtdError("ERROR_2", "Error message 2", BAD_REQUEST)

      val errorWrapper = ErrorWrapper("correlationId", error1)

      val result = errorWrapper.containsAnyOf(error2)
      result shouldBe false
    }
  }

}
