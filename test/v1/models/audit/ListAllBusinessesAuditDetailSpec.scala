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

package v1.models.audit

import play.api.libs.json.Json
import support.UnitSpec

class ListAllBusinessesAuditDetailSpec extends UnitSpec {

  val nino = "ZG903729C"

  "writes" must {
    "work" when {
      "success response" in {
        val json = Json.parse(s"""{
                                 |    "userType": "Agent",
                                 |    "agentReferenceNumber":"012345678",
                                 |    "nino": "$nino",
                                 |    "X-CorrelationId": "a1e8057e-fbbc-47a8-a8b478d9f015c253",
                                 |    "response": {
                                 |      "httpStatus": 201,
                                 |      "body": {
                                 |        "listOfBusinesses":[
                                 |        {
                                 |          "typeOfBusiness": "self-employment",
                                 |          "businessId": "123456789012345",
                                 |          "tradingName": "RCDTS",
                                 |          "links":[
                                 |            {
                                 |              "href":"/individuals/business/details/AA123456A/123456789012345",
                                 |              "method":"GET",
                                 |              "rel":"retrieve-business-details"
                                 |            }
                                 |          ]
                                 |        }
                                 |      ],
                                 |      "links":[
                                 |        {
                                 |          "href":"/individuals/business/details/AA123456A/list",
                                 |          "method":"GET",
                                 |          "rel":"self"
                                 |        }
                                 |      ]
                                 |    }
                                 |  }
                                 |}""".stripMargin)

        Json.toJson(
          ListAllBusinessesAuditDetail(
            userType = "Agent",
            agentReferenceNumber = Some("012345678"),
            nino = nino,
            `X-CorrelationId` = "a1e8057e-fbbc-47a8-a8b478d9f015c253",
            response = AuditResponse(
              201,
              Right(Some(Json.parse(s"""{
                                       |  "listOfBusinesses":[
                                       |    {
                                       |      "typeOfBusiness": "self-employment",
                                       |      "businessId": "123456789012345",
                                       |      "tradingName": "RCDTS",
                                       |      "links":[
                                       |        {
                                       |          "href":"/individuals/business/details/AA123456A/123456789012345",
                                       |          "method":"GET",
                                       |          "rel":"retrieve-business-details"
                                       |        }
                                       |      ]
                                       |    }
                                       |  ],
                                       |  "links":[
                                       |    {
                                       |      "href":"/individuals/business/details/AA123456A/list",
                                       |      "method":"GET",
                                       |      "rel":"self"
                                       |    }
                                       |  ]
                                       |}""".stripMargin)))
            )
          )) shouldBe json
      }
    }
  }
}
