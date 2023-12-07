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

package v1.models.response.retrieveBusinessDetails.downstream

import play.api.libs.json.Json
import support.UnitSpec

class BusinessAddressDetailsSpec extends UnitSpec {

  "BusinessAddressDetails" when {
    "read from JSON" must {
      "work" in {
        Json.parse(
          """
            |{
            |    "addressLine1": "100 SuttonStreet",
            |    "addressLine2": "Wokingham",
            |    "addressLine3": "Surrey",
            |    "addressLine4": "London",
            |    "postalCode": "DH14EJ",
            |    "countryCode": "GB"
            |}
            |""".stripMargin
        ).as[BusinessAddressDetails] shouldBe
          BusinessAddressDetails(
            addressLine1 = "100 SuttonStreet",
            addressLine2 = Some("Wokingham"),
            addressLine3 = Some("Surrey"),
            addressLine4 = Some("London"),
            postalCode = Some("DH14EJ"),
            countryCode = "GB"
          )

      }
    }
  }
}
