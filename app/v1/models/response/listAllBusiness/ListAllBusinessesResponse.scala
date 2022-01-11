/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.response.listAllBusiness

import cats.Functor
import config.AppConfig
import play.api.libs.json.{JsPath, Json, OWrites, Reads, Writes}
import v1.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v1.models.hateoas.{HateoasData, Link}
import v1.models.hateoas.RelType.RETRIEVE_BUSINESS_DETAILS

case class ListAllBusinessesResponse[I](listOfBusinesses: Seq[I])

object ListAllBusinessesResponse extends HateoasLinks {
  implicit def reads: Reads[ListAllBusinessesResponse[Business]] = {
    val businessDataReads: Reads[Seq[Business]] =
      (JsPath \ "businessData").readNullable[Seq[Business]](Business.readsSeqBusinessData).map(_.getOrElse(Nil))
    val propertyDataReads: Reads[Seq[Business]] =
      (JsPath \ "propertyData").readNullable[Seq[Business]](Business.readsSeqPropertyData).map(_.getOrElse(Nil))

    for {
      businessData <- businessDataReads
      propertyData <- propertyDataReads
    } yield {
      ListAllBusinessesResponse(businessData ++ propertyData)
    }
  }

  implicit def writes[I: Writes]: OWrites[ListAllBusinessesResponse[I]] = Json.writes[ListAllBusinessesResponse[I]]

  implicit object LinksFactory extends HateoasListLinksFactory[ListAllBusinessesResponse, Business, ListAllBusinessesHateoasData] {
    override def links(appConfig: AppConfig, data: ListAllBusinessesHateoasData): Seq[Link] = {
      Seq(
        listAllBusinesses(appConfig, data.nino)
      )
    }

    override def itemLinks(appConfig: AppConfig, data: ListAllBusinessesHateoasData, item: Business): Seq[Link] =
      Seq(retrieveBusinessDetails(appConfig, data.nino, item.businessId, rel = RETRIEVE_BUSINESS_DETAILS))
  }

  implicit object ResponseFunctor extends Functor[ListAllBusinessesResponse] {
    override def map[A, B](fa: ListAllBusinessesResponse[A])(f: A => B): ListAllBusinessesResponse[B] =
      ListAllBusinessesResponse(fa.listOfBusinesses.map(f))
  }
}

case class ListAllBusinessesHateoasData(nino: String) extends HateoasData