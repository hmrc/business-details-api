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

package v2.retrieveAccountingType.model.response

import play.api.libs.json.{JsResultException, JsValue, Json}
import support.UnitSpec
import v2.common.models.AccountingType

class RetrieveAccountingTypeResponseSpec extends UnitSpec {

  private def validDownstreamJson(typeOfBusiness: String, accountingType: String): JsValue = Json.parse(
    s"""
       |{
       |  "$typeOfBusiness": [
       |    {
       |      "accountingType": "$accountingType"
       |    }
       |  ]
       |}
    """.stripMargin
  )

  private def validMtdJson(accountingType: String): JsValue = Json.parse(
    s"""
       |{
       |  "accountingType": "$accountingType"
       |}
    """.stripMargin
  )

  private def parsedBody(accountingType: AccountingType): RetrieveAccountingTypeResponse = RetrieveAccountingTypeResponse(accountingType)

  private val businessTypes: Seq[String]           = Seq("selfEmployments", "ukProperty", "foreignProperty")
  private val accountingTypes: Seq[AccountingType] = Seq(AccountingType.CASH, AccountingType.ACCRUAL)

  private val invalidDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "selfEmployments": [
      |    {
      |      "accountingType": "TEST"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  "RetrieveAccountingType" should {
    businessTypes.foreach { typeOfBusiness =>
      accountingTypes.foreach { accountingType =>
        val responseModel: RetrieveAccountingTypeResponse = parsedBody(accountingType)
        val downstreamJson: JsValue                       = validDownstreamJson(typeOfBusiness, accountingType.toString)

        s"correctly parse valid JSON with typeOfBusiness $typeOfBusiness and accountingType $accountingType" in {
          downstreamJson.as[RetrieveAccountingTypeResponse] shouldBe responseModel
        }

        s"write to flat JSON correctly for typeOfBusiness $typeOfBusiness and accountingType $accountingType" in {
          Json.toJson(responseModel) shouldBe validMtdJson(accountingType.toString)
        }
      }
    }

    "throw an error given invalid Json" in {
      assertThrows[JsResultException](invalidDownstreamJson.as[RetrieveAccountingTypeResponse])
    }
  }

}
