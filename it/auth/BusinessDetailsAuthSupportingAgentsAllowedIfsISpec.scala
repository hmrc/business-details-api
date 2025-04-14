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

package auth

import api.services.DownstreamStub
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}

class BusinessDetailsAuthSupportingAgentsAllowedIfsISpec extends AuthSupportingAgentsAllowedISpec {

  override def servicesConfig: Map[String, Any] =
    Map("feature-switch.ifs_hip_migration_1171.enabled" -> false) ++ super.servicesConfig

  val callingApiVersion = "1.0"

  val supportingAgentsAllowedEndpoint = "list-all-businesses"

  val mtdUrl = s"/$nino/list"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

  val downstreamUri = s"/registration/business-details/nino/$nino"

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET

  val maybeDownstreamResponseJson: Option[JsValue] = Some(Json.parse(s"""
      |{
      |   "safeId": "XE00001234567890",
      |   "nino": "$nino",
      |   "mtdbsa": "123456789012345",
      |   "propertyIncome": false,
      |   "businessData": [
      |      {
      |         "incomeSourceType": "1",
      |         "incomeSourceId": "123456789012345",
      |         "accountingPeriodStartDate": "2001-01-01",
      |         "accountingPeriodEndDate": "2001-01-01",
      |         "tradingName": "RCDTS",
      |         "businessAddressDetails": {
      |            "addressLine1": "100 SuttonStreet",
      |            "addressLine2": "Wokingham",
      |            "addressLine3": "Surrey",
      |            "addressLine4": "London",
      |            "postalCode": "DH14EJ",
      |            "countryCode": "GB"
      |         },
      |         "businessContactDetails": {
      |            "phoneNumber": "01332752856",
      |            "mobileNumber": "07782565326",
      |            "faxNumber": "01332754256",
      |            "emailAddress": "stephen@manncorpone.co.uk"
      |         },
      |         "tradingStartDate": "2001-01-01",
      |         "cashOrAccruals": false,
      |         "seasonal": true,
      |         "cessationDate": "2001-01-01",
      |         "cessationReason": "002",
      |         "paperLess": true
      |      }
      |   ]
      |}
      |""".stripMargin))

}
