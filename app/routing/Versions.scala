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
import play.api.libs.json._
import play.api.mvc.{Headers, RequestHeader}

object Version {

  implicit val versionWrites: Writes[Version] = implicitly[Writes[String]].contramap[Version](_.name)

  implicit val versionReads: Reads[Version] = implicitly[Reads[String]].map(Version(_))
}

final case class Version(name: String) {
  override def toString: String = name
}

object Versions {
  val Version1: Version = Version("1.0")

  private val versionRegex = """application/vnd.hmrc.(\d.\d)\+json""".r

  def getFromRequest(request: RequestHeader): Either[GetFromRequestError, Version] =
    getFrom(request.headers).map(Version(_))

  private def getFrom(headers: Headers): Either[GetFromRequestError, String] =
    headers.get(ACCEPT).collect { case versionRegex(value) => value }.toRight(left = InvalidHeader)

}

sealed trait GetFromRequestError
case object InvalidHeader extends GetFromRequestError
