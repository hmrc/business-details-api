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

package v2.createUpdatePeriodsOfAccount.model.request

import play.api.libs.json.{JsObject, Json}
import support.UnitSpec
import v2.common.models.PeriodsOfAccountDates
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequestBody

class CreateUpdatePeriodsOfAccountRequestBodySpec extends UnitSpec {

  private val validVendorRequestWithPOA = Json.parse("""
      |{
      |  "periodsOfAccount": true,
      |  "periodsOfAccountDates" : [
      |    {
      |      "startDate": "2024-04-06",
      |      "endDate": "2025-04-05"
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val validVendorRequestWithoutPOA = Json.parse("""
      |{
      |  "periodsOfAccount": false
      |}
      |""".stripMargin)

  private val downstreamJsonWithPOA = Json.parse("""
      |{
      |  "periodsOfAccountDates" : [
      |    {
      |      "startDate": "2024-04-06",
      |      "endDate": "2025-04-05"
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val downstreamJsonWithoutPOA = Json.parse("""
      |{
      |  "periodsOfAccount": false
      |}
      |""".stripMargin)

  private val invalidVendorReqeuest1 = Json.parse("""
      |{
      |  "periodsOfAccount": false,
      |  "periodsOfAccountDates" : [
      |    {
      |      "startDate": "2024-04-06",
      |      "endDate": "2025-04-05"
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val invalidVendorReqeuest2 = Json.parse("""
      |{
      |  "periodsOfAccount": true
      |}
      |""".stripMargin)

  private val parsedRequestBodyPOA = CreateUpdatePeriodsOfAccountRequestBody(true, Some(Seq(PeriodsOfAccountDates("2024-04-06", "2025-04-05"))))

  private val parsedRequestBodyWithoutPOA = CreateUpdatePeriodsOfAccountRequestBody(false, None)
  private val parsedInvalidRequestBody1 = CreateUpdatePeriodsOfAccountRequestBody(false, Some(Seq(PeriodsOfAccountDates("2024-04-06", "2025-04-05"))))
  private val parsedInvalidRequestBody2 = CreateUpdatePeriodsOfAccountRequestBody(true, None)

  "CreateUpdatePeriodsOfAccountRequestBody" when {
    "periods of account is true and dates are provided" should {
      "read from json" in {
        validVendorRequestWithPOA.as[CreateUpdatePeriodsOfAccountRequestBody] shouldBe parsedRequestBodyPOA
      }
      "write just the dates to json" in {
        Json.toJson(parsedRequestBodyPOA) shouldBe downstreamJsonWithPOA
      }
    }
    "periods of account is false and dates are not provided" should {
      "read from json" in {
        validVendorRequestWithoutPOA.as[CreateUpdatePeriodsOfAccountRequestBody] shouldBe parsedRequestBodyWithoutPOA
      }
      "write to json" in {
        Json.toJson(parsedRequestBodyWithoutPOA) shouldBe downstreamJsonWithoutPOA
      }
    }
    "periods of account is false and dates are provided" should {
      "read from json" in {
        invalidVendorReqeuest1.as[CreateUpdatePeriodsOfAccountRequestBody] shouldBe parsedInvalidRequestBody1
      }
      // validator should ensure this scenario cannot happen
      "write an empty json object" in {
        Json.toJson(parsedInvalidRequestBody1) shouldBe JsObject.empty
      }
    }
    "periods of account is true and no dates are provided" should {
      "read from json" in {
        invalidVendorReqeuest2.as[CreateUpdatePeriodsOfAccountRequestBody] shouldBe parsedInvalidRequestBody2
      }
      // validator should ensure this scenario cannot happen
      "write an empty json object" in {
        Json.toJson(parsedInvalidRequestBody2) shouldBe JsObject.empty
      }
    }
  }

}
