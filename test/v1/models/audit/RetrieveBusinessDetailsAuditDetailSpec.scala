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

class RetrieveBusinessDetailsAuditDetailSpec extends UnitSpec {

  val nino = "ZG903729C"
  val businessId = "XAIS123456789012"

  "writes" must {
    "work" when {
      "success response" in {
        val json = Json.parse(s"""{
                                 |    "userType": "Agent",
                                 |    "agentReferenceNumber":"012345678",
                                 |    "nino": "$nino",
                                 |    "X-CorrelationId": "a1e8057e-fbbc-47a8-a8b478d9f015c253",
                                 |    "response": {
                                 |      "httpStatus": 200,
                                 |      "body": {
                                 |     "businessId": "XAIS123456789012",
                                 |     "typeOfBusiness": "self-employment",
                                 |     "tradingName": "RCDTS",
                                 |     "accountingPeriods": [
                                 |       {
                                 |         "start": "2001-01-01",
                                 |         "end": "2001-01-01"
                                 |     }
                                 |     ],
                                 |     "accountingType": "CASH",
                                 |     "commencementDate": "2001-01-01",
                                 |     "cessationDate": "2001-01-01",
                                 |     "businessAddressLineOne": "100 SuttonStreet",
                                 |     "businessAddressLineTwo": "Wokingham",
                                 |     "businessAddressLineThree": "Surrey",
                                 |     "businessAddressLineFour": "London",
                                 |     "businessAddressPostcode": "DH14EJ",
                                 |     "businessAddressCountryCode": "GB",
                                 |     "links": [
                                 |       {
                                 |         "href": "/individuals/business/details/$nino/$businessId",
                                 |         "method": "GET",
                                 |         "rel": "self"
                                 |       }
                                 |     ]
                                 |    }
                                 |  }
                                 |}""".stripMargin)

        Json.toJson(
          RetrieveBusinessDetailsAuditDetail(
            userType = "Agent",
            agentReferenceNumber = Some("012345678"),
            nino = nino,
            `X-CorrelationId` = "a1e8057e-fbbc-47a8-a8b478d9f015c253",
            response = AuditResponse(
              200,
              Right(Some(Json.parse(s"""{
                                       |   "businessId": "XAIS123456789012",
                                       |   "typeOfBusiness": "self-employment",
                                       |   "tradingName": "RCDTS",
                                       |   "accountingPeriods": [
                                       |     {
                                       |       "start": "2001-01-01",
                                       |       "end": "2001-01-01"
                                       |     }
                                       |   ],
                                       |   "accountingType": "CASH",
                                       |   "commencementDate": "2001-01-01",
                                       |   "cessationDate": "2001-01-01",
                                       |   "businessAddressLineOne": "100 SuttonStreet",
                                       |   "businessAddressLineTwo": "Wokingham",
                                       |   "businessAddressLineThree": "Surrey",
                                       |   "businessAddressLineFour": "London",
                                       |   "businessAddressPostcode": "DH14EJ",
                                       |   "businessAddressCountryCode": "GB",
                                       |   "links": [
                                       |     {
                                       |       "href": "/individuals/business/details/$nino/$businessId",
                                       |       "method": "GET",
                                       |       "rel": "self"
                                       |     }
                                       |   ]
                                       |}""".stripMargin)))
            )
          )) shouldBe json
      }
    }
  }
}
