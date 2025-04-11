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

import cats.data.Validated
import cats.implicits.catsSyntaxValidatedId
import com.typesafe.config.{Config, ConfigValue}
import config.Deprecation.{Deprecated, NotDeprecated}
import play.api.{ConfigLoader, Configuration}
import routing.Version
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField
import java.util
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

trait AppConfig {

  def appName: String
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

  // HIP Config
  def hipBaseUrl: String
  def hipEnv: String
  def hipToken: String
  def hipEnvironmentHeaders: Option[Seq[String]]

  // api2089
  def api2089BaseUrl: String
  def api2089Env: String
  def api2089Token: String
  def api2089EnvironmentHeaders: Option[Seq[String]]

  lazy val desDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = desBaseUrl, env = desEnv, token = desToken, environmentHeaders = desEnvironmentHeaders)

  lazy val ifsDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = ifsBaseUrl, env = ifsEnv, token = ifsToken, environmentHeaders = ifsEnvironmentHeaders)

  lazy val hipDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = hipBaseUrl, env = hipEnv, token = hipToken, environmentHeaders = hipEnvironmentHeaders)

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
  def endpointAllowsSupportingAgents(endpointName: String): Boolean

  def apiDocumentationUrl: String

  def deprecationFor(version: Version): Validated[String, Deprecation]

  def allowRequestCannotBeFulfilledHeader(version: Version): Boolean
}

@Singleton
class AppConfigImpl @Inject() (config: ServicesConfig, protected[config] val configuration: Configuration) extends AppConfig {

  def appName: String = config.getString("appName")

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

  // HIP Config
  val hipBaseUrl: String                         = config.baseUrl("hip")
  val hipEnv: String                             = config.getString("microservice.services.hip.env")
  val hipToken: String                           = config.getString("microservice.services.hip.token")
  val hipEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.hip.environmentHeaders")

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

  def allowRequestCannotBeFulfilledHeader(version: Version): Boolean =
    config.getBoolean(s"api.$version.endpoints.allow-request-cannot-be-fulfilled-header")

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

  def endpointAllowsSupportingAgents(endpointName: String): Boolean =
    supportingAgentEndpoints.getOrElse(endpointName, false)

  private val supportingAgentEndpoints: Map[String, Boolean] =
    configuration
      .getOptional[Map[String, Boolean]]("api.supporting-agent-endpoints")
      .getOrElse(Map.empty)

  def apiDocumentationUrl: String =
    configuration
      .get[Option[String]]("api.documentation-url")
      .getOrElse(s"https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/$appName")

  private val DATE_FORMATTER = new DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 23)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 59)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 59)
    .toFormatter()

  def deprecationFor(version: Version): Validated[String, Deprecation] = {
    val isApiDeprecated: Boolean = apiStatus(version) == "DEPRECATED"

    val deprecatedOn: Option[LocalDateTime] =
      configuration
        .getOptional[String](s"api.$version.deprecatedOn")
        .map(value => LocalDateTime.parse(value, DATE_FORMATTER))

    val sunsetDate: Option[LocalDateTime] =
      configuration
        .getOptional[String](s"api.$version.sunsetDate")
        .map(value => LocalDateTime.parse(value, DATE_FORMATTER))

    val isSunsetEnabled: Boolean =
      configuration.getOptional[Boolean](s"api.$version.sunsetEnabled").getOrElse(true)

    if (isApiDeprecated) {
      (deprecatedOn, sunsetDate, isSunsetEnabled) match {
        case (Some(dO), Some(sD), true) =>
          if (sD.isAfter(dO))
            Deprecated(dO, Some(sD)).valid
          else
            s"sunsetDate must be later than deprecatedOn date for a deprecated version $version".invalid
        case (Some(dO), None, true) => Deprecated(dO, Some(dO.plusMonths(6).plusDays(1))).valid
        case (Some(dO), _, false)   => Deprecated(dO, None).valid
        case _                      => s"deprecatedOn date is required for a deprecated version $version".invalid
      }

    } else NotDeprecated.valid

  }

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
