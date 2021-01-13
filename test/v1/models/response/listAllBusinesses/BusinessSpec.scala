/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.listAllBusinesses

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.domain.TypeOfBusiness
import v1.models.response.listAllBusiness.Business

class BusinessSpec extends UnitSpec {
  "readsBusinessData" should {

    val mtdModel: TypeOfBusiness => Business = mtdValue => Business(mtdValue, "Business", Some("Business Co"))

    "read from JSON" when {

      val desJson: JsValue = Json.parse(
        s"""
           |{
           |  "incomeSourceType": "doesn't matter",
           |  "incomeSourceId": "Business",
           |  "tradingName": "Business Co"
           |}
           |""".stripMargin)
      s"trading name is provided" in {
        desJson.as[Business](Business.readsBusinessData) shouldBe mtdModel(TypeOfBusiness.`self-employment`)
      }

      "no tradingName is provided" in {
        val desJson: JsValue = Json.parse(
          s"""
             |{
             |  "incomeSourceId": "Business"
             |}
             |""".stripMargin)

        val mtdModel: Business = Business(TypeOfBusiness.`self-employment`, "Business", None)

        desJson.as[Business](Business.readsBusinessData) shouldBe mtdModel
      }
    }
  }
  "readsSeqBusinessData" should {
    "read from JSON" when {

      val desJson: JsValue = Json.parse(
        s"""
           |[
           |  {
           |    "incomeSourceId": "Business",
           |    "tradingName": "Business Co"
           |  },
           |  {
           |    "incomeSourceType": "doesn't matter",
           |    "incomeSourceId": "Business"
           |  }
           |]
           |""".stripMargin)
      s"a json array is provided" in {
        desJson.as[Seq[Business]](Business.readsSeqBusinessData) shouldBe Seq(
          Business(TypeOfBusiness.`self-employment`, "Business", Some("Business Co")),
          Business(TypeOfBusiness.`self-employment`, "Business", None)
        )
      }
    }
  }

  "readsPropertyData" should {

    "read from JSON" when {
      s"incomeSourceType is uk-property" in {
        val desJson: JsValue = Json.parse(
          s"""
             |{
             |  "incomeSourceType": "uk-property",
             |  "incomeSourceId": "Business",
             |  "tradingName": "doesn't matter"
             |}
             |""".stripMargin)
        val mtdModel: Business = Business(TypeOfBusiness.`uk-property`, "Business", None)

        desJson.as[Business](Business.readsPropertyData) shouldBe mtdModel
      }
      s"incomeSourceType is foreign-property" in {
        val desJson: JsValue = Json.parse(
          s"""
             |{
             |  "incomeSourceType": "foreign-property",
             |  "incomeSourceId": "Business",
             |  "tradingName": "doesn't matter"
             |}
             |""".stripMargin)
        val mtdModel: Business = Business(TypeOfBusiness.`foreign-property`, "Business", None)

        desJson.as[Business](Business.readsPropertyData) shouldBe mtdModel
      }
      s"incomeSourceType is not provided" in {
        val desJson: JsValue = Json.parse(
          s"""
             |{
             |  "incomeSourceId": "Business",
             |  "tradingName": "doesn't matter"
             |}
             |""".stripMargin)
        val mtdModel: Business = Business(TypeOfBusiness.`property-unspecified`, "Business", None)

        desJson.as[Business](Business.readsPropertyData) shouldBe mtdModel
      }
    }
  }
  "readsSeqPropertyData" should {
    "read from JSON" when {

      val desJson: JsValue = Json.parse(
        s"""
           |[
           |  {
           |    "incomeSourceId": "Business",
           |    "tradingName": "doesn't matter"
           |  },
           |  {
           |    "incomeSourceType": "uk-property",
           |    "incomeSourceId": "Business"
           |  }
           |]
           |""".stripMargin)
      s"a json array is provided" in {
        desJson.as[Seq[Business]](Business.readsSeqPropertyData) shouldBe Seq(
          Business(TypeOfBusiness.`property-unspecified`, "Business", None),
          Business(TypeOfBusiness.`uk-property`, "Business", None)
        )
      }
    }
  }

  "writes" should {
    "write to JSON" when {
      "passed a model" in {
        val model = Business(TypeOfBusiness.`self-employment`, "myid", Some("name"))
        val json = Json.parse(
          """
            |{
            |  "typeOfBusiness": "self-employment",
            |  "businessId": "myid",
            |  "tradingName": "name"
            |}
            |""".stripMargin)

        Json.toJson(model) shouldBe json
      }
    }
  }
}