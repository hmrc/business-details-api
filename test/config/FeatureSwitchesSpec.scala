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

  "FeatureSwitches" should {
    "return true" when {
      "the feature switch is set to true" in {
        val config = Configuration(
          "ifs.enabled"                              -> true,
          "endpoint-2089.enabled"                    -> true,
          "scp005a_QuarterlyTypeChoice.enabled"      -> true,
          "supporting-agents-access-control.enabled" -> true
        )

        val featureSwitches = FeatureSwitches(config)

        featureSwitches.isIfsEnabled shouldBe true
        featureSwitches.isEndpoint2089Enabled shouldBe true
        featureSwitches.isScp005aQuarterlyTypeChoiceEnabled shouldBe true
        featureSwitches.supportingAgentsAccessControlEnabled shouldBe true
      }

      "the feature switch is not present in the config" in {
        val config = Configuration.empty

        val featureSwitches = FeatureSwitches(config)

        featureSwitches.isIfsEnabled shouldBe true
        featureSwitches.isEndpoint2089Enabled shouldBe true
        featureSwitches.isScp005aQuarterlyTypeChoiceEnabled shouldBe true
      }
    }

    "return false" when {
      "the feature switch is set to false" in {
        val config = Configuration(
          "ifs.enabled"                         -> false,
          "endpoint-2089.enabled"               -> false,
          "scp005a_QuarterlyTypeChoice.enabled" -> false
        )

        val featureSwitches = FeatureSwitches(config)

        featureSwitches.isIfsEnabled shouldBe false
        featureSwitches.isEndpoint2089Enabled shouldBe false
        featureSwitches.isScp005aQuarterlyTypeChoiceEnabled shouldBe false
      }
    }
  }

}
