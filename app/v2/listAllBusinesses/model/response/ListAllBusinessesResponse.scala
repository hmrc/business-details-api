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

package v2.listAllBusinesses.model.response

import cats.Functor
import play.api.libs.json.*
import v2.retrieveBusinessDetails.model.response.downstream.RetrieveBusinessDetailsDownstreamResponse

case class ListAllBusinessesResponse[I](listOfBusinesses: Seq[I])

object ListAllBusinessesResponse {

  def fromDownstream(downstreamResponse: RetrieveBusinessDetailsDownstreamResponse): ListAllBusinessesResponse[Business] =
    ListAllBusinessesResponse(
      downstreamResponse.businessData.getOrElse(Nil).map(Business.fromDownstreamBusiness) :++
        downstreamResponse.propertyData.getOrElse(Nil).map(Business.fromDownstreamProperty)
    )

  implicit def writes[I: Writes]: OWrites[ListAllBusinessesResponse[I]] = Json.writes[ListAllBusinessesResponse[I]]

  implicit object ResponseFunctor extends Functor[ListAllBusinessesResponse] {

    override def map[A, B](fa: ListAllBusinessesResponse[A])(f: A => B): ListAllBusinessesResponse[B] =
      ListAllBusinessesResponse(fa.listOfBusinesses.map(f))

  }

}
