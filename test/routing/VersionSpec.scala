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
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest
import support.UnitSpec

class VersionSpec extends UnitSpec {

  "serialized to Json" must {

    "return the expected Json output" in {
      Json.toJson(Version("1.2")) shouldBe JsString("1.2")
    }
  }

  "Version" when {
    "retrieved from a request header" must {
      "return the specified version" in {
        Version.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.1.2+json"))) shouldBe Right(Version("1.2"))
      }
    }

    "return InvalidHeader when the version header is missing" in {
      Version.getFromRequest(FakeRequest().withHeaders()) shouldBe Left(InvalidHeader)
    }

    "return an error if the Accept header value is invalid" in {
      Version.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/XYZ.2.0+json"))) shouldBe Left(InvalidHeader)
    }
  }

  "VersionReads" should {
    "successfully read Version" in {
      JsString("1.2").as[Version] shouldBe Version("1.2")
    }
  }

  "toString" should {
    "return the version name" in {
      Version("1.2").toString shouldBe "1.2"
    }
  }

}
