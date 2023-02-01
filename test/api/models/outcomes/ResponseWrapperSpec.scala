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

package api.models.outcomes

import support.UnitSpec

class ResponseWrapperSpec extends UnitSpec {

  "mapping a ResponseWrapper" should {
    "return the the response mapped by applied partial function" in {
      val partialFunction: Int => String = _.toString

      val response         = ResponseWrapper("X-123", 1)
      val expectedResponse = ResponseWrapper("X-123", "1")

      response.map(partialFunction) shouldBe expectedResponse
    }
  }

}
