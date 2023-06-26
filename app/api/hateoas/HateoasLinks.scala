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

package api.hateoas

import api.models.hateoas.Link
import api.models.hateoas.Method.GET
import api.models.hateoas.RelType.SELF
import config.AppConfig

trait HateoasLinks {

  // API resource links
  def retrieveBusinessDetails(appConfig: AppConfig, nino: String, businessId: String, rel: String = SELF): Link =
    Link(href = retrieveBusinessDetailsUri(appConfig, nino, businessId), method = GET, rel = rel)

  // Domain URIs
  private def retrieveBusinessDetailsUri(appConfig: AppConfig, nino: String, businessId: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/$businessId"

  def listAllBusinesses(appConfig: AppConfig, nino: String): Link =
    Link(href = listAllBusinessesUri(appConfig, nino), method = GET, rel = SELF)

  private def listAllBusinessesUri(appConfig: AppConfig, nino: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/list"

}
