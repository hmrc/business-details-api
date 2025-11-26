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

package v1.listAllBusinesses.model.response

import api.hateoas.Method.GET
import api.hateoas.{HateoasFactory, HateoasWrapper, Link}
import api.models.domain.TypeOfBusiness
import api.models.domain.TypeOfBusiness.*
import config.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v1.retrieveBusinessDetails.model.response.downstream.{BusinessData, PropertyData, RetrieveBusinessDetailsDownstreamResponse}

class ListAllBusinessesResponseSpec extends UnitSpec {

  "ListAllBusinessesResponse" must {
    "correctly convert from the downstream data model" when {

      val incomeSourceId  = "someIncomeSourceId"
      val yearOfMigration = Some("ignoredYear")

      def downstream(businessData: Option[Seq[BusinessData]] = None,
                     propertyData: Option[Seq[PropertyData]] = None): RetrieveBusinessDetailsDownstreamResponse =
        RetrieveBusinessDetailsDownstreamResponse(yearOfMigration, businessData, propertyData)

      def downstreamBusiness(tradingName: Option[String]): BusinessData =
        BusinessData(
          incomeSourceId = incomeSourceId,
          accountingPeriodStartDate = "ignoredStart",
          accountingPeriodEndDate = "ignoredEnd",
          tradingName = tradingName,
          businessAddressDetails = None,
          tradingStartDate = None,
          cashOrAccruals = None,
          cessationDate = None,
          firstAccountingPeriodStartDate = None,
          firstAccountingPeriodEndDate = None,
          latencyDetails = None,
          quarterTypeElection = None
        )

      def downstreamProperty(typeOfBusiness: Option[TypeOfBusiness]): PropertyData =
        PropertyData(
          incomeSourceType = typeOfBusiness,
          incomeSourceId = incomeSourceId,
          accountingPeriodStartDate = "ignoredStart",
          accountingPeriodEndDate = "ignoredEnd",
          tradingStartDate = None,
          cashOrAccruals = None,
          cessationDate = None,
          firstAccountingPeriodStartDate = None,
          firstAccountingPeriodEndDate = None,
          latencyDetails = None,
          quarterTypeElection = None
        )

      "given a single property business" must {
        behave like singlePropertyTest(Some(`foreign-property`), `foreign-property`)
        behave like singlePropertyTest(Some(`uk-property`), `uk-property`)
        behave like singlePropertyTest(None, `property-unspecified`)

        def singlePropertyTest(downstreamTypeOfBusiness: Option[TypeOfBusiness], expectedTypeOfBusiness: TypeOfBusiness): Unit =
          s"return single entry with that type of business $downstreamTypeOfBusiness" in {
            ListAllBusinessesResponse.fromDownstream(downstream(propertyData = Some(Seq(downstreamProperty(downstreamTypeOfBusiness))))) shouldBe
              ListAllBusinessesResponse(Seq(Business(expectedTypeOfBusiness, incomeSourceId, None)))
          }
      }

      "given a self-employment business" must {
        behave like singleBusinessTest(Some("tradingName"))
        behave like singleBusinessTest(None)
      }

      def singleBusinessTest(tradingName: Option[String]): Unit =
        s"return a response with a single self-employment entry and trading name $tradingName" in {
          ListAllBusinessesResponse.fromDownstream(downstream(businessData = Some(Seq(downstreamBusiness(tradingName))))) shouldBe
            ListAllBusinessesResponse(Seq(Business(`self-employment`, incomeSourceId, tradingName)))
        }

      "given multiple properties and self-employment businesses" must {
        "return multiple businesses" in {
          ListAllBusinessesResponse.fromDownstream(downstream(
            propertyData = Some(Seq(
              downstreamProperty(Some(`foreign-property`))
            )),
            businessData = Some(
              Seq(
                downstreamBusiness(Some("businessA")),
                downstreamBusiness(Some("businessB"))
              ))
          )) shouldBe
            ListAllBusinessesResponse(
              Seq(
                Business(`self-employment`, incomeSourceId, Some("businessA")),
                Business(`self-employment`, incomeSourceId, Some("businessB")),
                Business(`foreign-property`, incomeSourceId, None)
              ))
        }
      }

      "given an empty propertyData Seq" must {
        "be treated as if it is absent (i.e. None)" in {
          ListAllBusinessesResponse.fromDownstream(
            downstream(
              propertyData = Some(Nil),
              businessData = Some(Seq(downstreamBusiness(None)))
            )) shouldBe
            ListAllBusinessesResponse(Seq(Business(`self-employment`, incomeSourceId, None)))
        }
      }

      "given an empty businessData Seq" must {
        "be treated as if it is absent (i.e. None)" in {
          ListAllBusinessesResponse.fromDownstream(
            downstream(
              propertyData = Some(Seq(downstreamProperty(None))),
              businessData = Some(Nil)
            )) shouldBe
            ListAllBusinessesResponse(Seq(Business(`property-unspecified`, incomeSourceId, None)))
        }
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
