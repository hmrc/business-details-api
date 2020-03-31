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

package v1.models.response

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.domain.TypeOfBusiness
import v1.models.response.listAllBusiness.Business

class BusinessSpec extends UnitSpec {
  "reads" should {

    val mtdModel: TypeOfBusiness => Business = mtdValue => Business(mtdValue, "Business", Some("Business Co"))

    "read from JSON" when {

      val desJson: String => JsValue = desValue => Json.parse(
        s"""
           |{
           |  "incomeSourceType": "$desValue",
           |  "incomeSourceId": "Business",
           |  "tradingName": "Business Co"
           |}
           |""".stripMargin)
      Business
      Seq(("1", TypeOfBusiness.`self-employment`), ("2", TypeOfBusiness.`uk-property`), ("3", TypeOfBusiness.`foreign-property`)).foreach {
        case (desValue, mtdValue) =>
          s"the field contains [$desValue]" in {
            desJson(desValue).as[Business] shouldBe mtdModel(mtdValue)
          }
      }
    }

    "read from JSON with no tradingName" in {
      val desJson: JsValue = Json.parse(
        s"""
           |{
           |  "incomeSourceType": "1",
           |  "incomeSourceId": "Business"
           |}
           |""".stripMargin)

      val mtdModel: Business = Business(TypeOfBusiness.`self-employment`, "Business", None)

      desJson.as[Business] shouldBe mtdModel
    }
  }
}