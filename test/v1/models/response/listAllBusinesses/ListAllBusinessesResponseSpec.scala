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

package v1.models.response.listAllBusinesses

import api.hateoas.{HateoasFactory, HateoasWrapper, Link}
import api.models.domain.TypeOfBusiness
import api.hateoas.Method.GET
import mocks.MockAppConfig
import play.api.libs.json.{Json, Reads}
import support.UnitSpec
import v1.models.response.listAllBusiness.ListAllBusinessesResponse.getReads
import v1.models.response.listAllBusiness.{Business, ListAllBusinessesHateoasData, ListAllBusinessesResponse}
import v1.models.response.listAllBusinesses.ListAllBusinessJson._

class ListAllBusinessesResponseSpec extends UnitSpec {

  "reads" should {
    "output a DES model" when {
      val isIfsEnabled                                                       = false
      implicit val responseReads: Reads[ListAllBusinessesResponse[Business]] = getReads(isIfsEnabled)

      "passed DES json with businessData" in {

        val desJson = Json.parse(
          desResponseWithBusinessData.stripMargin
        )

        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("RCDTS")),
            Business(TypeOfBusiness.`self-employment`, "098765432109876", Some("RCDTS 2"))
          ))

        desJson.as[ListAllBusinessesResponse[Business]] shouldBe model
      }

      "passed DES json with propertyData" in {
        val desJson = Json.parse(desResponseWithPropertyData.stripMargin)
        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`uk-property`, "123456789012345", None),
            Business(TypeOfBusiness.`foreign-property`, "098765432109876", None)
          ))
        desJson.as[ListAllBusinessesResponse[Business]] shouldBe model
      }

      "passed DES json with businessData and propertyData" in {
        val desJson = Json.parse(desResponseWithPropertyAndBusinessData.stripMargin)
        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("RCDTS")),
            Business(TypeOfBusiness.`self-employment`, "098765432109876", Some("RCDTS 2")),
            Business(TypeOfBusiness.`uk-property`, "123456789012345", None),
            Business(TypeOfBusiness.`foreign-property`, "098765432109876", None)
          ))
        desJson.as[ListAllBusinessesResponse[Business]] shouldBe model
      }
    }

    "output an IFS model" when {
      val isIfsEnabled                                                       = true
      implicit val responseReads: Reads[ListAllBusinessesResponse[Business]] = getReads(isIfsEnabled)

      "passed IFS json with businessData" in {

        val desJson = Json.parse(
          ifsResponseWithBusinessData.stripMargin
        )

        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("RCDTS")),
            Business(TypeOfBusiness.`self-employment`, "098765432109876", Some("RCDTS 2"))
          ))

        desJson.as[ListAllBusinessesResponse[Business]] shouldBe model
      }

      "passed IFS json with propertyData" in {
        val desJson = Json.parse(ifsResponseWithPropertyData.stripMargin)
        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`uk-property`, "123456789012345", None),
            Business(TypeOfBusiness.`foreign-property`, "098765432109876", None)
          ))
        desJson.as[ListAllBusinessesResponse[Business]] shouldBe model
      }

      "passed IFS json with businessData and propertyData" in {
        val desJson = Json.parse(ifsResponseWithPropertyAndBusinessData.stripMargin)
        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("RCDTS")),
            Business(TypeOfBusiness.`self-employment`, "098765432109876", Some("RCDTS 2")),
            Business(TypeOfBusiness.`uk-property`, "123456789012345", None),
            Business(TypeOfBusiness.`foreign-property`, "098765432109876", None)
          ))
        desJson.as[ListAllBusinessesResponse[Business]] shouldBe model
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return mtd JSON" in {
        val model = ListAllBusinessesResponse(
          Seq(
            Business(TypeOfBusiness.`self-employment`, "123456789012345", Some("name")),
            Business(TypeOfBusiness.`uk-property`, "123456789012346", None)
          ))
        val mtdJson = Json.parse(s"""
             |{
             |  "listOfBusinesses": [
             |    {
             |      "typeOfBusiness": "self-employment",
             |      "businessId": "123456789012345",
             |      "tradingName": "name"
             |    },
             |    {
             |      "typeOfBusiness": "uk-property",
             |      "businessId": "123456789012346"
             |    }
             |  ]
             |}
             |""".stripMargin)
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

  "HateoasFactory" must {
    class Test extends MockAppConfig {
      val hateoasFactory = new HateoasFactory(mockAppConfig)
      val nino           = "someNino"
      MockedAppConfig.apiGatewayContext.returns("individuals/business/details").anyNumberOfTimes()
    }

    "expose the correct links for list" in new Test {
      hateoasFactory.wrapList(
        ListAllBusinessesResponse(Seq(Business(TypeOfBusiness.`self-employment`, "myid", None))),
        ListAllBusinessesHateoasData(nino)) shouldBe
        HateoasWrapper(
          ListAllBusinessesResponse(
            Seq(
              HateoasWrapper(
                Business(TypeOfBusiness.`self-employment`, "myid", None),
                Seq(Link(s"/individuals/business/details/$nino/myid", GET, "retrieve-business-details"))))),
          Seq(
            Link(s"/individuals/business/details/$nino/list", GET, "self")
          )
        )
    }
  }

}
