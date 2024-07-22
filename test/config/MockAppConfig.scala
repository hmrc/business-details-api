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

import org.scalamock.handlers.{CallHandler, CallHandler0}
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import routing.Version

trait MockAppConfig extends MockFactory {

  implicit val mockAppConfig: AppConfig = mock[AppConfig]

  object MockedAppConfig {
    // MTD ID Lookup Config
    def mtdIdBaseUrl: CallHandler[String] = (() => mockAppConfig.mtdIdBaseUrl: String).expects()

    // Des config
    def desBaseUrl: CallHandler[String]                         = (() => mockAppConfig.desBaseUrl).expects()
    def desToken: CallHandler[String]                           = (() => mockAppConfig.desToken).expects()
    def desEnvironment: CallHandler[String]                     = (() => mockAppConfig.desEnv).expects()
    def desEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.desEnvironmentHeaders).expects()

    // Ifs config
    def ifsBaseUrl: CallHandler[String]                         = (() => mockAppConfig.ifsBaseUrl).expects()
    def ifsToken: CallHandler[String]                           = (() => mockAppConfig.ifsToken).expects()
    def ifsEnvironment: CallHandler[String]                     = (() => mockAppConfig.ifsEnv).expects()
    def ifsEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.ifsEnvironmentHeaders).expects()

    // api2089 config
    def api2089BaseUrl: CallHandler[String]                         = (() => mockAppConfig.api2089BaseUrl).expects()
    def api2089Token: CallHandler[String]                           = (() => mockAppConfig.api2089Token).expects()
    def api2089Environment: CallHandler[String]                     = (() => mockAppConfig.api2089Env).expects()
    def api2089EnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.api2089EnvironmentHeaders).expects()

    // API Config
    def featureSwitches: CallHandler[Configuration]              = (() => mockAppConfig.featureSwitches: Configuration).expects()
    def apiGatewayContext: CallHandler[String]                   = (() => mockAppConfig.apiGatewayContext).expects()
    def apiStatus(version: Version): CallHandler[String]         = (mockAppConfig.apiStatus: Version => String).expects(version)
    def endpointsEnabled(version: String): CallHandler[Boolean]  = (mockAppConfig.endpointsEnabled(_: String)).expects(version)
    def endpointsEnabled(version: Version): CallHandler[Boolean] = (mockAppConfig.endpointsEnabled(_: Version)).expects(version)

    def apiVersionReleasedInProduction(version: String): CallHandler[Boolean] =
      (mockAppConfig.apiVersionReleasedInProduction: String => Boolean).expects(version)

    def endpointReleasedInProduction(version: String, key: String): CallHandler[Boolean] =
      (mockAppConfig.endpointReleasedInProduction: (String, String) => Boolean).expects(version, key)

    def confidenceLevelConfig: CallHandler0[ConfidenceLevelConfig] =
      (() => mockAppConfig.confidenceLevelConfig).expects()

    def confidenceLevelCheckEnabled: CallHandler[ConfidenceLevelConfig] =
      (() => mockAppConfig.confidenceLevelConfig: ConfidenceLevelConfig).expects()

    def endpointAllowsSecondaryAgents(endpointName: String): CallHandler[Boolean] =
      (mockAppConfig.endpointAllowsSecondaryAgents(_: String)).expects(endpointName)

  }

}
