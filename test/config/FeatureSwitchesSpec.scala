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

package config

import play.api.Configuration
import support.UnitSpec

class FeatureSwitchesSpec extends UnitSpec {

  private val configuration = Configuration(
    "feature-switch.enabled" -> true
  )

  private val featureSwitches = FeatureSwitches(configuration)

  "FeatureSwitches" should {
    "return true" when {
      "the feature switch is set to true" in {
        featureSwitches.featureSwitchConfig.getOptional[Boolean]("feature-switch.enabled") shouldBe Some(true)
      }
    }
    "return false" when {
      "the feature switch is not present in the config" in {
        featureSwitches.featureSwitchConfig.getOptional[Boolean]("invalid") shouldBe None
      }
    }
  }

}
