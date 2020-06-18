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

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class IncomeSourceTypeSpec extends UnitSpec with EnumJsonSpecSupport {
  testRoundTrip[IncomeSourceType](
    ("1", IncomeSourceType.`1`),
    ("2", IncomeSourceType.`2`),
    ("3", IncomeSourceType.`3`)
  )

  "toTypeOfBusiness" should {
    Seq(
      (IncomeSourceType.`1`, TypeOfBusiness.`self-employment`),
      (IncomeSourceType.`2`, TypeOfBusiness.`uk-property`),
      (IncomeSourceType.`3`, TypeOfBusiness.`foreign-property`),
    ).foreach {
      case (incomeSourceType, typeOfBusiness) =>
        s"convert $incomeSourceType to $typeOfBusiness" in {
          incomeSourceType.toTypeOfBusiness shouldBe typeOfBusiness
        }
    }
  }

}
