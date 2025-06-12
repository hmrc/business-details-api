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

package v2.fixtures

import play.api.libs.json.{JsValue, Json}
import v2.common.models.PeriodsOfAccountDates
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequestBody

object CreateUpdatePeriodsOfAccountFixtures {

  val fullRequestBodyModel: CreateUpdatePeriodsOfAccountRequestBody =
    CreateUpdatePeriodsOfAccountRequestBody(
      periodsOfAccount = true,
      periodsOfAccountDates = Some(
        Seq(
          PeriodsOfAccountDates(startDate = "2025-04-06", endDate = "2025-07-05"),
          PeriodsOfAccountDates(startDate = "2025-07-06", endDate = "2025-10-05")
        )
      )
    )

  val minimumRequestBodyModel: CreateUpdatePeriodsOfAccountRequestBody =
    CreateUpdatePeriodsOfAccountRequestBody(
      periodsOfAccount = false,
      periodsOfAccountDates = None
    )

  val validFullRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "periodsOfAccount": true,
      |  "periodsOfAccountDates": [
      |    {
      |      "startDate": "2025-04-06",
      |      "endDate": "2025-07-05"
      |    },
      |    {
      |      "startDate": "2025-07-06",
      |      "endDate": "2025-10-05"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val validMinimumRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "periodsOfAccount": false
      |}
    """.stripMargin
  )

}
