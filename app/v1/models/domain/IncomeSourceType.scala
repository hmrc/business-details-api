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

package v1.models.domain

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait IncomeSourceType {
  def toTypeOfBusiness: TypeOfBusiness
}

object IncomeSourceType {

  case object `1` extends IncomeSourceType {
    override def toTypeOfBusiness: TypeOfBusiness = TypeOfBusiness.`self-employment`
  }

  case object `2` extends IncomeSourceType {
    override def toTypeOfBusiness: TypeOfBusiness = TypeOfBusiness.`uk-property`
  }

  case object `3` extends IncomeSourceType {
    override def toTypeOfBusiness: TypeOfBusiness = TypeOfBusiness.`foreign-property`
  }

  implicit val format: Format[IncomeSourceType] = Enums.format[IncomeSourceType]
  val parser: PartialFunction[String, IncomeSourceType] = Enums.parser[IncomeSourceType]
}