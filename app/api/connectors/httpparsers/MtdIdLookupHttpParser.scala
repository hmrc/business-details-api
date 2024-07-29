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

package api.connectors.httpparsers

import api.connectors.MtdIdLookupConnector
import play.api.http.Status.{FORBIDDEN, INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.libs.json.{Reads, __}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object MtdIdLookupHttpParser extends HttpParser {

  private val mtdIdJsonReads: Reads[String] = (__ \ "mtdbsa").read[String]

  implicit val mtdIdLookupHttpReads: HttpReads[MtdIdLookupConnector.Outcome] = (_: String, _: String, response: HttpResponse) => {
    response.status match {
      case OK           => Right(response.json.as[String](mtdIdJsonReads))
      case FORBIDDEN    => Left(MtdIdLookupConnector.Error(FORBIDDEN))
      case UNAUTHORIZED => Left(MtdIdLookupConnector.Error(UNAUTHORIZED))
      case _            => Left(MtdIdLookupConnector.Error(INTERNAL_SERVER_ERROR))

      // case status => Left(MtdIdLookupConnector.Error(status))
    }
  }

}
