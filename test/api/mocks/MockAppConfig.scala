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

package api.mocks

import config.{AppConfig, ConfidenceLevelConfig}
import org.scalamock.handlers.{CallHandler, CallHandler0}
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import routing.Version

trait MockAppConfig extends MockFactory {
  val mockAppConfig: AppConfig = mock[AppConfig]

  object MockAppConfig {

    //  MTD ID Lookup Config
    def mtdIdBaseUrl: CallHandler0[String] = (() => mockAppConfig.mtdIdBaseUrl: String).expects()

    // DES Config
    def desBaseUrl: CallHandler0[String] = (() => mockAppConfig.desBaseUrl: String).expects()

    def desEnv: CallHandler[String] = (() => mockAppConfig.desEnv: String).expects()

    def desToken: CallHandler[String] = (() => mockAppConfig.desToken: String).expects()

    def desEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.desEnvironmentHeaders: Option[Seq[String]]).expects()

    // API Config
    def apiGatewayContext: CallHandler[String] = (() => mockAppConfig.apiGatewayContext: String).expects()

    def confidenceLevelConfig: CallHandler[ConfidenceLevelConfig] =
      (() => mockAppConfig.confidenceLevelConfig: ConfidenceLevelConfig).expects()

    def apiStatus(version: Version): CallHandler[String] = (mockAppConfig.apiStatus(_: Version)).expects(version)

    def featureSwitches: CallHandler[Configuration] = (() => mockAppConfig.featureSwitches: Configuration).expects()

    def endpointsEnabled(version: String): CallHandler[Boolean]  = (mockAppConfig.endpointsEnabled(_: String)).expects(version)

    def apiVersionReleasedInProduction(version: String): CallHandler[Boolean] =
      (mockAppConfig.apiVersionReleasedInProduction: String => Boolean).expects(version)

    def endpointReleasedInProduction(version: String, key: String): CallHandler[Boolean] =
      (mockAppConfig.endpointReleasedInProduction: (String, String) => Boolean).expects(version, key)

    def confidenceLevelCheckEnabled: CallHandler[ConfidenceLevelConfig] =
      (() => mockAppConfig.confidenceLevelConfig: ConfidenceLevelConfig).expects()


  }

}
