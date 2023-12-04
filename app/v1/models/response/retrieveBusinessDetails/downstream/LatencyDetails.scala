/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package v1.models.response.retrieveBusinessDetails.downstream

import api.models.domain.TaxYear
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class LatencyDetails(latencyEndDate: String,
                          taxYear1: TaxYear,
                          latencyIndicator1: LatencyIndicator,
                          taxYear2: TaxYear,
                          latencyIndicator2: LatencyIndicator)

object LatencyDetails {
  implicit val writes: OWrites[LatencyDetails] = Json.writes[LatencyDetails]

  implicit val taxYearReads: Reads[TaxYear] = implicitly[Reads[String]].map(TaxYear.fromDownstream)
  implicit val reads: Reads[LatencyDetails] = (
    (JsPath \ "latencyEndDate").read[String] and
      (JsPath \ "taxYear1").read[TaxYear] and
      (JsPath \ "latencyIndicator1").read[LatencyIndicator] and
      (JsPath \ "taxYear2").read[TaxYear] and
      (JsPath \ "latencyIndicator2").read[LatencyIndicator]
    )(LatencyDetails.apply _)

}