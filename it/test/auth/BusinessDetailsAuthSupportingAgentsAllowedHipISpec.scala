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

class BusinessDetailsAuthSupportingAgentsAllowedHipISpec extends AuthSupportingAgentsAllowedISpec {

  val callingApiVersion = "1.0"

  val supportingAgentsAllowedEndpoint = "list-all-businesses"

  val mtdUrl = s"/$nino/list"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

  val downstreamUri = "/etmp/RESTAdapter/itsa/taxpayer/business-details"

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET

  override val downstreamQueryParams: Map[String, String] = Map("nino" -> nino)

  val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.parse(
      s"""
        |{
        |  "success": {
        |    "processingDate": "2023-07-05T09:16:58Z",
        |    "taxPayerDisplayResponse": {
        |      "safeId": "XAIS123456789012",
        |      "nino": "$nino",
        |      "mtdId": "XNIT00000068707",
        |      "yearOfMigration": "2023",
        |      "propertyIncomeFlag": false,
        |      "businessData": [
        |        {
        |          "incomeSourceId": "XAIS12345678901",
        |          "incomeSource": "ITSB",
        |          "accPeriodSDate": "2001-01-01",
        |          "accPeriodEDate": "2001-01-01",
        |          "tradingName": "RCDTS",
        |          "businessAddressDetails": {
        |            "addressLine1": "100 SuttonStreet",
        |            "addressLine2": "Wokingham",
        |            "addressLine3": "Surrey",
        |            "addressLine4": "London",
        |            "postalCode": "DH14EJ",
        |            "countryCode": "GB"
        |          },
        |          "businessContactDetails": {
        |            "telephone": "01332752856",
        |            "mobileNo": "07782565326",
        |            "faxNo": "01332754256",
        |            "email": "stephen@manncorpone.co.uk"
        |          },
        |          "tradingSDate": "2001-01-01",
        |          "contextualTaxYear": "2024",
        |          "seasonalFlag": true,
        |          "cessationDate": "2001-01-01",
        |          "paperLessFlag": true,
        |          "incomeSourceStartDate": "2010-03-14",
        |          "firstAccountingPeriodStartDate": "2018-04-06",
        |          "firstAccountingPeriodEndDate": "2018-12-12",
        |          "latencyDetails": {
        |            "latencyEndDate": "2018-12-12",
        |            "taxYear1": "2018",
        |            "latencyIndicator1": "A",
        |            "taxYear2": "2019",
        |            "latencyIndicator2": "Q"
        |          },
        |          "quarterTypeElection": {
        |            "quarterReportingType": "STANDARD",
        |            "taxYearofElection": "2023"
        |          }
        |        }
        |      ]
        |    }
        |  }
        |}
      """.stripMargin
    )
  )

}
