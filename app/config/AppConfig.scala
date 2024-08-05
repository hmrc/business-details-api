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

import com.typesafe.config.{Config, ConfigValue}
import play.api.{ConfigLoader, Configuration}
import routing.Version
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

trait AppConfig {
  // MTD ID Lookup Config
  def mtdIdBaseUrl: String

  // DES Config
  def desBaseUrl: String
  def desEnv: String
  def desToken: String
  def desEnvironmentHeaders: Option[Seq[String]]

  // IFS Config
  def ifsBaseUrl: String
  def ifsEnv: String
  def ifsToken: String
  def ifsEnvironmentHeaders: Option[Seq[String]]

  // api2089
  def api2089BaseUrl: String
  def api2089Env: String
  def api2089Token: String
  def api2089EnvironmentHeaders: Option[Seq[String]]

  lazy val desDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = desBaseUrl, env = desEnv, token = desToken, environmentHeaders = desEnvironmentHeaders)

  lazy val ifsDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = ifsBaseUrl, env = ifsEnv, token = ifsToken, environmentHeaders = ifsEnvironmentHeaders)

  lazy val api2089DownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = api2089BaseUrl, env = api2089Env, token = api2089Token, environmentHeaders = api2089EnvironmentHeaders)

  // API Config
  def apiGatewayContext: String
  def confidenceLevelConfig: ConfidenceLevelConfig
  def apiStatus(version: Version): String

  def featureSwitches: Configuration
  def endpointsEnabled(version: String): Boolean
  def endpointsEnabled(version: Version): Boolean
  def safeEndpointsEnabled(version: String): Boolean

  /** Currently only for OAS documentation.
    */
  def apiVersionReleasedInProduction(version: String): Boolean

  /** Currently only for OAS documentation.
    */
  def endpointReleasedInProduction(version: String, name: String): Boolean

  /** Defaults to false
    */
  def endpointAllowsSecondaryAgents(endpointName: String): Boolean
}

@Singleton
class AppConfigImpl @Inject() (config: ServicesConfig, protected[config] val configuration: Configuration) extends AppConfig {

  // MTD ID Lookup Config
  val mtdIdBaseUrl: String                      = config.baseUrl(serviceName = "mtd-id-lookup")
  val keyValuesJ: util.Map[String, ConfigValue] = configuration.entrySet.toMap.asJava
  // DES Config
  val desBaseUrl: String                         = config.baseUrl("des")
  val desEnv: String                             = config.getString("microservice.services.des.env")
  val desToken: String                           = config.getString("microservice.services.des.token")
  val desEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.des.environmentHeaders")

  // IFS Config
  val ifsBaseUrl: String                         = config.baseUrl("ifs")
  val ifsEnv: String                             = config.getString("microservice.services.ifs.env")
  val ifsToken: String                           = config.getString("microservice.services.ifs.token")
  val ifsEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.ifs.environmentHeaders")

  // Api2089 Config
  val api2089BaseUrl: String                         = config.baseUrl("api2089")
  val api2089Env: String                             = config.getString("microservice.services.api2089.env")
  val api2089Token: String                           = config.getString("microservice.services.api2089.token")
  val api2089EnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.api2089.environmentHeaders")

  // API Config
  val apiGatewayContext: String                    = config.getString("api.gateway.context")
  val confidenceLevelConfig: ConfidenceLevelConfig = configuration.get[ConfidenceLevelConfig](s"api.confidence-level-check")
  def apiStatus(version: Version): String          = config.getString(s"api.${version.name}.status")
  def featureSwitches: Configuration               = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)
  def endpointsEnabled(version: String): Boolean   = config.getBoolean(s"api.$version.endpoints.enabled")
  def endpointsEnabled(version: Version): Boolean  = config.getBoolean(s"api.${version.name}.endpoints.enabled")

  /** Like endpointsEnabled, but will return false if version doesn't exist.
    */
  def safeEndpointsEnabled(version: String): Boolean =
    configuration
      .getOptional[Boolean](s"api.$version.endpoints.enabled")
      .getOrElse(false)

  def apiVersionReleasedInProduction(version: String): Boolean =
    confBoolean(
      path = s"api.$version.endpoints.api-released-in-production",
      defaultValue = false
    )

  def endpointReleasedInProduction(version: String, name: String): Boolean =
    apiVersionReleasedInProduction(version) &&
      confBoolean(
        path = s"api.$version.endpoints.released-in-production.$name",
        defaultValue = true
      )

  /** Can't use config.getConfBool as it's typesafe, and the app-config files use strings.
    */
  private def confBoolean(path: String, defaultValue: Boolean): Boolean =
    if (configuration.underlying.hasPath(path)) config.getBoolean(path) else defaultValue

  def endpointAllowsSecondaryAgents(endpointName: String): Boolean = secondaryAgentEndpoints.getOrElse(endpointName, false)

  private val secondaryAgentEndpoints: Map[String, Boolean] =
    configuration
      .getOptional[Map[String, Boolean]]("api.secondary-agent-endpoints")
      .getOrElse(Map.empty)

}

case class ConfidenceLevelConfig(confidenceLevel: ConfidenceLevel, definitionEnabled: Boolean, authValidationEnabled: Boolean)

object ConfidenceLevelConfig {

  implicit val configLoader: ConfigLoader[ConfidenceLevelConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    ConfidenceLevelConfig(
      confidenceLevel = ConfidenceLevel.fromInt(config.getInt("confidence-level")).getOrElse(ConfidenceLevel.L200),
      definitionEnabled = config.getBoolean("definition.enabled"),
      authValidationEnabled = config.getBoolean("auth-validation.enabled")
    )
  }

}
