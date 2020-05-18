/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.retrieveBusinessDetails.des

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import v1.models.domain.accountingType.{AccountingType, CashOrAccruals}
import v1.models.domain.{IncomeSourceType, TypeOfBusiness}
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}

case class BusinessDetails(businessId: String,
                           typeOfBusiness: TypeOfBusiness,
                           tradingName: Option[String],
                           accountingPeriods: Option[Seq[AccountingPeriod]],
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
    businessAddressCountryCode = businessAddressCountryCode
  )
}

object BusinessDetails {
  implicit val reads: Reads[BusinessDetails] = (
    (JsPath \ "incomeSourceId").read[String] and
      (JsPath \ "incomeSourceType").read[IncomeSourceType].map(_.toTypeOfBusiness) and
      (JsPath \ "incomeSourceType").readNullable[String] and
      (JsPath \ "accountingPeriods").readNullable[Seq[AccountingPeriod]] and
      (JsPath \ "cashOrAccruals").read[CashOrAccruals].map(_.toMtd) and
      (JsPath \ "tradingStartDate").readNullable[String] and
      (JsPath \ "cessationDate").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "addressLine1").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "addressLine2").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "addressLine3").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "addressLine4").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "postalCode").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "countryCode").readNullable[String]
  )(BusinessDetails.apply _)
}
