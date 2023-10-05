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

package routing

import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json.{JsError, JsResult, JsString, JsSuccess, JsValue, Json}
import play.api.test.FakeRequest
import routing.Version.{VersionReads, VersionWrites}
import support.UnitSpec

class VersionSpec extends UnitSpec {

  "serialized to Json" must {
    "return the expected Json output" in {
      val version: Version = Version1
      val expected         = Json.parse(""" "1.0" """)
      val result           = Json.toJson(version)
      result shouldBe expected
    }
  }

  "Versions" when {
    "retrieved from a request header" must {
      "return the specified version" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))) shouldBe Right(Version1)
      }

      "return an error if the version is unsupported" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.3.0+json"))) shouldBe Left(VersionNotFound)
      }

      "return InvalidHeader when the version header is missing" in {
        Versions.getFromRequest(FakeRequest().withHeaders()) shouldBe Left(InvalidHeader)
      }

      "return an error if the Accept header value is invalid" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/XYZ.2.0+json"))) shouldBe Left(InvalidHeader)
      }
    }
  }

  "VersionReads" should {
    "successfully read Version2" in {
      val versionJson: JsValue      = JsString(Version1.name)
      val result: JsResult[Version] = VersionReads.reads(versionJson)

      result shouldEqual JsSuccess(Version1)
    }

    "return error for unrecognised version" in {
      val versionJson: JsValue      = JsString("UnknownVersion")
      val result: JsResult[Version] = VersionReads.reads(versionJson)

      result shouldBe a[JsError]
    }
  }

}
