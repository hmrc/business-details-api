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

package stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.libs.json.{JsObject, Json}
import support.WireMockMethods

object AuthStub extends WireMockMethods {

  private val authoriseUri: String = "/auth/authorise"

  private val mtdEnrolment = List(
    Json.obj(
      "key" -> "HMRC-MTD-IT",
      "identifiers" -> Json.arr(
        Json.obj(
          "key"   -> "MTDITID",
          "value" -> "1234567890"
        )
      )
    )
  )

  private val secondaryAgentEnrolments = List(
    Json.obj(
      "key" -> "HMRC-MTD-IT-SECONDARY",
      "identifiers" -> Json.arr(
        Json.obj(
          "key"   -> "MTDITID",
          "value" -> "1234567890"
        )
      ),
      "delegatedAuthRule" -> "mtd-it-auth-secondary"
    ),
    Json.obj(
      "key" -> "HMRC-AS-AGENT",
      "identifiers" -> Json.arr(
        Json.obj(
          "key"   -> "AgentReferenceNumber",
          "value" -> "123567890"
        )
      ))
  )

  def authorised(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = OK, body = successfulAuthResponse(mtdEnrolment))
  }

  def authorisedAsSecondaryAgent(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = OK, body = successfulAuthResponse(secondaryAgentEnrolments, "Agent"))
  }

  private def successfulAuthResponse(enrolments: Seq[JsObject], affinityGroup: String = "Individual"): JsObject = {
    Json.obj("authorisedEnrolments" -> enrolments, "affinityGroup" -> "Individual", "affinityGroup" -> affinityGroup)
  }

  def unauthorisedNotLoggedIn(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = UNAUTHORIZED, headers = Map("WWW-Authenticate" -> """MDTP detail="MissingBearerToken""""))
  }

  def unauthorisedOther(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = UNAUTHORIZED, headers = Map("WWW-Authenticate" -> """MDTP detail="InvalidBearerToken""""))
  }

  def unauthorisedAsSecondaryAgent(): StubMapping = {
    when(method = POST, uri = authoriseUri)
      .thenReturn(status = UNAUTHORIZED, headers = Map("WWW-Authenticate" -> """MDTP detail="FailedRelationship""""))
  }


}
