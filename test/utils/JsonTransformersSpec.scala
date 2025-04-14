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

package utils

import play.api.libs.json.{JsObject, JsPath, JsString, JsSuccess, Json, Reads}
import support.UnitSpec
import utils.JsonTransformers.{conditionalCopy, conditionalUpdate}

class JsonTransformersSpec extends UnitSpec {

  "JsonTransformers" when {
    ".conditionalUpdate" should {
      val transformer: Reads[JsObject] = conditionalUpdate(
        path = JsPath \ "incomeSourceType",
        transform = {
          case JsString("02") => JsString("uk-property")
          case other          => other
        }
      )

      "transform the value at the specified path if the value meets the transformation condition" in {
        val json: JsObject = Json.obj(
          "incomeSourceType" -> "02",
          "otherField"       -> "test"
        )

        val expected: JsObject = Json.obj(
          "incomeSourceType" -> "uk-property",
          "otherField"       -> "test"
        )

        transformer.reads(json).get shouldBe JsSuccess(expected).value
      }

      "not transform if the value at the specified path does not meet the transformation condition" in {
        val json: JsObject = Json.obj(
          "incomeSourceType" -> "foreign-property",
          "otherField"       -> "test"
        )

        transformer.reads(json).get shouldBe JsSuccess(json).value
      }

      "not transform if the specified path does not exist" in {
        val json: JsObject = Json.obj(
          "otherField" -> "test"
        )

        transformer.reads(json).get shouldBe JsSuccess(json).value
      }
    }

    ".conditionalCopy" should {
      val transformer: Reads[JsObject] = conditionalCopy(
        sourcePath = JsPath \ "sourceField",
        targetPath = JsPath \ "targetField"
      )

      "copy the value from source to target if both exist" in {
        val json: JsObject = Json.obj(
          "sourceField" -> "some-value",
          "targetField" -> "some-other-value",
          "otherField"  -> "test"
        )

        val expected: JsObject = Json.obj(
          "sourceField" -> "some-value",
          "targetField" -> "some-value",
          "otherField"  -> "test"
        )

        transformer.reads(json).get shouldBe JsSuccess(expected).value
      }

      "create and copy the value from source to target if target does not exist" in {
        val json: JsObject = Json.obj(
          "sourceField" -> "some-value",
          "otherField"  -> "test"
        )

        val expected: JsObject = Json.obj(
          "sourceField" -> "some-value",
          "targetField" -> "some-value",
          "otherField"  -> "test"
        )

        transformer.reads(json).get shouldBe JsSuccess(expected).value
      }

      "not create the target if both do not exist" in {
        val json: JsObject = Json.obj(
          "otherField" -> "test"
        )

        transformer.reads(json).get shouldBe JsSuccess(json).value
      }

      "not copy the value from source to target if the source does not exist" in {
        val json: JsObject = Json.obj(
          "targetField" -> "some-other-value",
          "otherField"  -> "test"
        )

        transformer.reads(json).get shouldBe JsSuccess(json).value
      }
    }
  }

}
