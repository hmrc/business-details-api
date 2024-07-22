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

import com.google.inject.ImplementedBy
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@ImplementedBy(classOf[FeatureSwitchesImpl])
trait FeatureSwitches {

  def isIfsEnabled: Boolean
  def isEndpoint2089Enabled: Boolean
  def isScp005aQuarterlyTypeChoiceEnabled: Boolean
  def secondaryAgentsAccessControlEnabled: Boolean

  def isEnabled(key: String): Boolean
  def isReleasedInProduction(feature: String): Boolean
}

@Singleton
class FeatureSwitchesImpl(featureSwitchConfig: Configuration) extends FeatureSwitches {

  @Inject
  def this(appConfig: AppConfig) = this(appConfig.featureSwitches)

  val isIfsEnabled: Boolean                        = isEnabled("ifs")
  val isEndpoint2089Enabled: Boolean               = isEnabled("endpoint-2089")
  val isScp005aQuarterlyTypeChoiceEnabled: Boolean = isEnabled("scp005a_QuarterlyTypeChoice")
  val secondaryAgentsAccessControlEnabled: Boolean = isEnabled("secondary-agents-access-control")

  def isEnabled(key: String): Boolean = isConfigTrue(key + ".enabled")

  def isReleasedInProduction(feature: String): Boolean = isConfigTrue(feature + ".released-in-production")

  private def isConfigTrue(key: String): Boolean = featureSwitchConfig.getOptional[Boolean](key).getOrElse(true)
}

object FeatureSwitches {
  def apply(configuration: Configuration): FeatureSwitches = new FeatureSwitchesImpl(configuration)

  def apply(appConfig: AppConfig): FeatureSwitches = new FeatureSwitchesImpl(appConfig)
}
