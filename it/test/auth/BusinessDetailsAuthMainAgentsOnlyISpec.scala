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

package auth

import api.models.domain.TaxYear
import api.services.DownstreamStub
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}

class BusinessDetailsAuthMainAgentsOnlyISpec extends AuthMainAgentsOnlyISpec {

  val callingApiVersion = "1.0"

  val supportingAgentsNotAllowedEndpoint = "create-amend-quarterly-period-type"

  private val businessId = "XAIS12345678901"
  private val taxYear    = TaxYear.fromMtd("2024-25")

  val mtdUrl = s"/$nino/$businessId/${taxYear.asMtd}"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.put(requestJson))

  val downstreamUri: String =
    s"/income-tax/${taxYear.asTysDownstream}/income-sources/reporting-type/$nino/$businessId"

  val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.parse("""
                 |{
                 | "QRT": "Standard"
                 |}
                 |""".stripMargin)
  )

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.PUT

  override val expectedMtdSuccessStatus: Int = NO_CONTENT

  private val requestJson = Json.parse("""
    |{
    | "quarterlyPeriodType": "standard"
    |}
    |""".stripMargin)

}
