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

package definition

import cats.implicits.catsSyntaxValidatedId
import config.Deprecation.NotDeprecated
import config.{ConfidenceLevelConfig, MockAppConfig}
import definition.APIStatus.{ALPHA, BETA}
import mocks.MockHttpClient
import play.api.Configuration
import routing.{Version1, Version2}
import support.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

class ApiDefinitionFactorySpec extends UnitSpec {

  class Test extends MockHttpClient with MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockedAppConfig.apiGatewayContext returns "individuals/business/details"
  }

  private val confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        MockedAppConfig.featureSwitches returns Configuration.empty
        MockedAppConfig.apiStatus(Version1) returns "BETA"
        MockedAppConfig.apiStatus(Version2) returns "BETA"
        MockedAppConfig.endpointsEnabled(Version1) returns true
        MockedAppConfig.endpointsEnabled(Version2) returns true
        MockedAppConfig.deprecationFor(Version1).returns(NotDeprecated.valid).anyNumberOfTimes()
        MockedAppConfig.deprecationFor(Version2).returns(NotDeprecated.valid).anyNumberOfTimes()
        (MockedAppConfig.confidenceLevelCheckEnabled returns ConfidenceLevelConfig(
          confidenceLevel = confidenceLevel,
          definitionEnabled = true,
          authValidationEnabled = true))
          .anyNumberOfTimes()

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Business Details (MTD)",
              description = "An API for providing business details data",
              context = "individuals/business/details",
              categories = Seq("INCOME_TAX_MTD"),
              versions = Seq(
                APIVersion(
                  version = Version1,
                  status = BETA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  version = Version2,
                  status = BETA,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "confidenceLevel" when {
    Seq(
      (true, ConfidenceLevel.L250, ConfidenceLevel.L250),
      (true, ConfidenceLevel.L200, ConfidenceLevel.L200),
      (false, ConfidenceLevel.L200, ConfidenceLevel.L50)
    ).foreach { case (definitionEnabled, configCL, expectedDefinitionCL) =>
      s"confidence-level-check.definition.enabled is $definitionEnabled and confidence-level = $configCL" should {
        s"return confidence level $expectedDefinitionCL" in new Test {
          MockedAppConfig.confidenceLevelCheckEnabled returns ConfidenceLevelConfig(
            confidenceLevel = configCL,
            definitionEnabled = definitionEnabled,
            authValidationEnabled = true)
          apiDefinitionFactory.confidenceLevel shouldBe expectedDefinitionCL
        }
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {
      "return the correct status" in new Test {
        MockedAppConfig.apiStatus(Version1) returns "BETA"
        MockedAppConfig.deprecationFor(Version1).returns(NotDeprecated.valid).anyNumberOfTimes()
        apiDefinitionFactory.buildAPIStatus(Version1) shouldBe BETA
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      "default to alpha" in new Test {
        MockedAppConfig.apiStatus(Version1) returns "ALPHO"
        MockedAppConfig.deprecationFor(Version1).returns(NotDeprecated.valid).anyNumberOfTimes()
        apiDefinitionFactory.buildAPIStatus(Version1) shouldBe ALPHA
      }
    }
  }

  "the 'deprecatedOn' parameter is missing for a deprecated version" should {
    "throw exception" in new Test {
      MockedAppConfig.apiStatus(Version1) returns "DEPRECATED"
      MockedAppConfig
        .deprecationFor(Version1)
        .returns("deprecatedOn date is required for a deprecated version".invalid)
        .anyNumberOfTimes()

      val exception: Exception = intercept[Exception] {
        apiDefinitionFactory.buildAPIStatus(Version1)
      }

      val exceptionMessage: String = exception.getMessage
      exceptionMessage shouldBe "deprecatedOn date is required for a deprecated version"
    }
  }

}
