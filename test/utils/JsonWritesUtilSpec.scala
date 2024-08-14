/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import play.api.libs.json._
import support.UnitSpec

object JsonWritesUtilSpec {
  trait D
  case class D1(field: String)  extends D
  case class D2(field: Int)     extends D
  case class D3(field: Boolean) extends D
}

class JsonWritesUtilSpec extends UnitSpec with JsonWritesUtil {
  import JsonWritesUtilSpec._

  "OWrites produced from writesFrom" when {
    implicit val writesOnlyForD1_D2: OWrites[D] = writesFrom {
      case d: D1 => Json.writes[D1].writes(d)
      case d: D2 => Json.writes[D2].writes(d)
    }

    "writing an object matched by the partial function" must {
      "correctly write to JSON" in {
        Json.toJson[D](D1("value")) shouldBe Json.obj("field" -> "value")
        Json.toJson[D](D2(1)) shouldBe Json.obj("field" -> 1)
      }
    }

    "writing an object not-matched by the partial function" must {
      "throw an exception" in {
        assertThrows[IllegalArgumentException](Json.toJson[D](D3(false)))
      }
    }
  }

  "filterNull" should {
    "filter out fields with JsNull values in a JsObject" in {
      val json = Json.obj(
        "field1" -> JsString("value1"),
        "field2" -> JsNull,
        "field3" -> JsNumber(42)
      )

      val expectedJson = Json.obj(
        "field1" -> JsString("value1"),
        "field3" -> JsNumber(42)
      )

      val result = filterNull(json)
      result shouldEqual expectedJson
    }

    "return an empty JsObject if all fields are JsNull" in {
      val json = Json.obj(
        "field1" -> JsNull,
        "field2" -> JsNull
      )

      val expectedJson = Json.obj()

      val result = filterNull(json)
      result shouldEqual expectedJson
    }

    "return the same JsObject if there are no JsNull values" in {
      val json = Json.obj(
        "field1" -> JsString("value1"),
        "field2" -> Json.obj(
          "nestedField1" -> JsNumber(42)
        )
      )

      val result = filterNull(json)
      result shouldEqual json
    }
  }

}
