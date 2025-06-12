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

import api.utils.JsonErrorValidators
import play.api.libs.json.Json
import support.UnitSpec
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequestBody
import v2.fixtures.CreateUpdatePeriodsOfAccountFixtures._

class CreateUpdatePeriodsOfAccountRequestBodySpec extends UnitSpec with JsonErrorValidators {

  "CreateUpdatePeriodsOfAccountRequestBody" when {
    "periods of account is true and dates are provided" should {
      "read from JSON" in {
        validFullRequestBodyJson.as[CreateUpdatePeriodsOfAccountRequestBody] shouldBe fullRequestBodyModel
      }

      "write only the dates to JSON" in {
        Json.toJson(fullRequestBodyModel) shouldBe validFullRequestBodyJson.removeProperty("/periodsOfAccount")
      }
    }

    "periods of account is false and dates are not provided" should {
      "read from JSON" in {
        validMinimumRequestBodyJson.as[CreateUpdatePeriodsOfAccountRequestBody] shouldBe minimumRequestBodyModel
      }

      "write to JSON" in {
        Json.toJson(minimumRequestBodyModel) shouldBe validMinimumRequestBodyJson
      }
    }
  }

}
