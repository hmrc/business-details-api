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

package api.models.audit

import api.models.auth.UserDetails
import api.models.errors.TaxYearFormatError
import config.MockAppConfig
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class FlattenedGenericAuditDetailSpec extends UnitSpec with MockAppConfig {

  val versionNumber: String = "1.0"
  val nino: String = "XX751130C"
  val businessId: String = "XBIS12345678901"
  val taxYear: String = "2021-22"
  val quarterlyPeriodType: String = "standard"
  val agentReferenceNumber: Option[String] = Some("012345678")
  val userType: String = "Agent"
  val userDetails: UserDetails = UserDetails("mtdId", userType, agentReferenceNumber)
  val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val auditDetailJsonSuccess: JsValue = Json.parse(
    s"""
       |{
       |    "versionNumber": "$versionNumber",
       |    "userType": "$userType",
       |    "agentReferenceNumber": "${agentReferenceNumber.get}",
       |    "nino": "$nino",
       |    "businessId": "$businessId",
       |    "taxYear": "$taxYear",
       |    "quarterlyPeriodType": "$quarterlyPeriodType",
       |    "X-CorrelationId": "$correlationId",
       |    "outcome": "success",
       |    "httpStatusCode": $OK
       |}
    """.stripMargin
  )


  val auditDetailModelSuccess: FlattenedGenericAuditDetail = FlattenedGenericAuditDetail(
    versionNumber = Some(versionNumber),
    userDetails = userDetails,
    params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear, "quarterlyPeriodType" -> quarterlyPeriodType),
    `X-CorrelationId` = correlationId,
    auditResponse = AuditResponse(
      httpStatus = OK,
      response = Right(Some(Json.obj()))
    )
  )

  val invalidTaxYearAuditDetailJson: JsValue = Json.parse(
    s"""
       |{
       |    "versionNumber": "$versionNumber",
       |    "userType": "$userType",
       |    "agentReferenceNumber": "${agentReferenceNumber.get}",
       |    "nino": "$nino",
       |    "businessId": "$businessId",
       |    "taxYear": "$taxYear",
       |    "quarterlyPeriodType": "$quarterlyPeriodType",
       |    "X-CorrelationId": "$correlationId",
       |    "outcome": "error",
       |    "httpStatusCode": $BAD_REQUEST,
       |    "errorCodes": ["FORMAT_TAX_YEAR"]
       |}
    """.stripMargin
  )

  val invalidTaxYearAuditDetailModel: FlattenedGenericAuditDetail = FlattenedGenericAuditDetail(
    versionNumber = Some(versionNumber),
    userDetails = userDetails,
    params = Map("nino" -> nino, "businessId" -> businessId, "taxYear" -> taxYear, "quarterlyPeriodType" -> quarterlyPeriodType),
    `X-CorrelationId` = correlationId,
    auditResponse = AuditResponse(
      httpStatus = BAD_REQUEST,
      response = Left(List(AuditError(TaxYearFormatError.code)))
    )
  )

  "FlattenedGenericAuditDetail" when {
    "written to JSON (success)" should {
      "produce the expected JsObject" in {
        Json.toJson(auditDetailModelSuccess) shouldBe auditDetailJsonSuccess
      }
    }

    "written to JSON (error)" should {
      "produce the expected JsObject" in {
        Json.toJson(invalidTaxYearAuditDetailModel) shouldBe invalidTaxYearAuditDetailJson
      }
    }
  }

}
