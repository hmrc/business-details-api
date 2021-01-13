/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.domain.accountingType.{AccountingType, CashOrAccruals}
import v1.models.domain.TypeOfBusiness
import v1.models.response.retrieveBusinessDetails.{AccountingPeriod, RetrieveBusinessDetailsResponse}

case class BusinessDetails(businessId: String,
                           typeOfBusiness: TypeOfBusiness,
                           tradingName: Option[String],
                           accountingPeriods: Seq[AccountingPeriod],
                           accountingType: Option[AccountingType],
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

  val cashOrAccrualsReads: Reads[Option[AccountingType]] = (JsPath \ "cashOrAccrualsFlag").readNullable[Boolean].map {
    case Some(false) => Some(AccountingType.CASH)
    case Some(true)  => Some(AccountingType.ACCRUALS)
    case None        => None
  }

  val accountingPeriodReads: Reads[Seq[AccountingPeriod]] = (
    (JsPath \ "accountingPeriodStartDate").read[String] and
      (JsPath \ "accountingPeriodEndDate").read[String]
    ).apply((start, end) =>  Seq(AccountingPeriod(start, end)))

  implicit val writes: OWrites[BusinessDetails] = Json.writes[BusinessDetails]

  val readsBusinessData: Reads[BusinessDetails] = (
    (JsPath \ "incomeSourceId").read[String] and
      Reads.pure(TypeOfBusiness.`self-employment`) and
      (JsPath \ "tradingName").readNullable[String] and
      accountingPeriodReads and
      (JsPath \ "cashOrAccruals").readNullable[CashOrAccruals].map(_.map(_.toMtd)) and
      (JsPath \ "tradingStartDate").readNullable[String] and
      (JsPath \ "cessationDate").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "addressLine1").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "addressLine2").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "addressLine3").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "addressLine4").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "postalCode").readNullable[String] and
      (JsPath \ "businessAddressDetails" \ "countryCode").readNullable[String]
  )(BusinessDetails.apply _)
  val readsSeqBusinessData: Reads[Seq[BusinessDetails]] = Reads.traversableReads[Seq, BusinessDetails](implicitly, readsBusinessData)

  val readsPropertyData: Reads[BusinessDetails] = (
    (JsPath \ "incomeSourceId").read[String] and
      (JsPath \ "incomeSourceType").readNullable[TypeOfBusiness].map(_.getOrElse(TypeOfBusiness.`property-unspecified`)) and
      Reads.pure(None) and
      accountingPeriodReads and
      cashOrAccrualsReads and
      (JsPath \ "tradingStartDate").readNullable[String] and
      (JsPath \ "cessationDate").readNullable[String] and
      Reads.pure(None) and
      Reads.pure(None) and
      Reads.pure(None) and
      Reads.pure(None) and
      Reads.pure(None) and
      Reads.pure(None)
    ) (BusinessDetails.apply _)
  val readsSeqPropertyData: Reads[Seq[BusinessDetails]] = Reads.traversableReads[Seq, BusinessDetails](implicitly, readsPropertyData)

  }
