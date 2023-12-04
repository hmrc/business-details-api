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

package v1.models.response.retrieveBusinessDetails.downstream

import api.models.domain.{AccountingType, TaxYear, TypeOfBusiness}
import config.FeatureSwitches
import play.api.libs.functional.syntax._
import play.api.libs.json._
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}


case class BusinessDetails(businessId: String,
                           typeOfBusiness: TypeOfBusiness,
                           tradingName: Option[String],
                           accountingPeriods: Seq[AccountingPeriod],
                           firstAccountingPeriodStartDate: Option[String],
                           firstAccountingPeriodEndDate: Option[String],
                           latencyDetails: Option[LatencyDetails],
                           yearOfMigration: Option[String],
                           accountingType: AccountingType,
                           commencementDate: Option[String],
                           cessationDate: Option[String],
                           businessAddressLineOne: Option[String],
                           businessAddressLineTwo: Option[String],
                           businessAddressLineThree: Option[String],
                           businessAddressLineFour: Option[String],
                           businessAddressPostcode: Option[String],
                           businessAddressCountryCode: Option[String]) {

  def toMtd: RetrieveBusinessDetailsResponse = RetrieveBusinessDetailsResponse(
    businessId = businessId,
    typeOfBusiness = typeOfBusiness,
    tradingName = tradingName,
    accountingPeriods = accountingPeriods,
    accountingType = accountingType,
    commencementDate = commencementDate,
    cessationDate = cessationDate,
    businessAddressLineOne = businessAddressLineOne,
    businessAddressLineTwo = businessAddressLineTwo,
    businessAddressLineThree = businessAddressLineThree,
    businessAddressLineFour = businessAddressLineFour,
    businessAddressPostcode = businessAddressPostcode,
    businessAddressCountryCode = businessAddressCountryCode,
    firstAccountingPeriodStartDate = firstAccountingPeriodStartDate,
    firstAccountingPeriodEndDate = firstAccountingPeriodEndDate,
    latencyDetails = latencyDetails,
    yearOfMigration = yearOfMigration
  )

}

object BusinessDetails {

  private def cashOrAccrualsReads(field: String)(implicit featureSwitches: FeatureSwitches): Reads[AccountingType] = {
    if (featureSwitches.isIfsEnabled || field == "cashOrAccrualsFlag") {
      (JsPath \ field).readNullable[Boolean].map {
        case Some(true) => AccountingType.ACCRUALS
        case Some(false) => AccountingType.CASH
        case None => AccountingType.CASH
      }
    } else {
      (JsPath \ field).readNullable[String].map {
        case Some("cash") => AccountingType.CASH
        case Some("accruals") => AccountingType.ACCRUALS
        case _ => AccountingType.CASH
      }
    }
  }

  private val accountingPeriodReads: Reads[Seq[AccountingPeriod]] = (
    (JsPath \ "accountingPeriodStartDate").read[String] and
      (JsPath \ "accountingPeriodEndDate").read[String]
    ).apply((start, end) => Seq(AccountingPeriod(start, end)))

  def readsBusinessData(implicit featureSwitches: FeatureSwitches): Reads[BusinessDetails] =
    (
      (JsPath \ "incomeSourceId").read[String] and
        Reads.pure(TypeOfBusiness.`self-employment`) and
        (JsPath \ "tradingName").readNullable[String] and
        accountingPeriodReads and
        (JsPath \ "firstAccountingPeriodStartDate").readNullable[String] and
        (JsPath \ "firstAccountingPeriodEndDate").readNullable[String] and
        (JsPath \ "latencyDetails").readNullable[LatencyDetails] and
        (JsPath \ "yearOfMigration").readNullable[String] and
        cashOrAccrualsReads("cashOrAccruals") and
        (JsPath \ "tradingStartDate").readNullable[String] and
        (JsPath \ "cessationDate").readNullable[String] and
        (JsPath \ "businessAddressDetails" \ "addressLine1").readNullable[String] and
        (JsPath \ "businessAddressDetails" \ "addressLine2").readNullable[String] and
        (JsPath \ "businessAddressDetails" \ "addressLine3").readNullable[String] and
        (JsPath \ "businessAddressDetails" \ "addressLine4").readNullable[String] and
        (JsPath \ "businessAddressDetails" \ "postalCode").readNullable[String] and
        (JsPath \ "businessAddressDetails" \ "countryCode").readNullable[String]
      )(BusinessDetails.apply _)

  private def cashOrAccrualsReadsForPropertyData(implicit featureSwitches: FeatureSwitches) = {
    if (featureSwitches.isIfsEnabled) {
      cashOrAccrualsReads("cashOrAccruals")
    } else {
      cashOrAccrualsReads("cashOrAccrualsFlag")
    }
  }

  def readsPropertyData(implicit featureSwitches: FeatureSwitches): Reads[BusinessDetails] =
    (
      (JsPath \ "incomeSourceId").read[String] and
        (JsPath \ "incomeSourceType").readNullable[TypeOfBusiness].map(_.getOrElse(TypeOfBusiness.`property-unspecified`)) and
        Reads.pure(None) and
        accountingPeriodReads and
        (JsPath \ "firstAccountingPeriodStartDate").readNullable[String] and
        (JsPath \ "firstAccountingPeriodEndDate").readNullable[String] and
        (JsPath \ "latencyDetails").readNullable[LatencyDetails] and
        (JsPath \ "yearOfMigration").readNullable[String] and
        cashOrAccrualsReadsForPropertyData and
        (JsPath \ "tradingStartDate").readNullable[String] and
        (JsPath \ "cessationDate").readNullable[String] and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None) and
        Reads.pure(None)
      )(BusinessDetails.apply _)

}
