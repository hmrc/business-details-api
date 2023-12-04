/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package v1.models.response.retrieveBusinessDetails.downstream

import play.api.libs.json._

trait LatencyIndicator

object LatencyIndicator {

  case object Annual extends LatencyIndicator {
    override def toString: String = "A"
  }

  case object Quarterly extends LatencyIndicator {
    override def toString: String = "Q"
  }

  implicit val writes: Writes[LatencyIndicator] = Writes { (latencyIndicator: LatencyIndicator) =>
    JsString(latencyIndicator.toString)
  }

  implicit val reads: Reads[LatencyIndicator] = Reads { json =>
    json.as[String] match {
      case "A" | "a" => JsSuccess(Annual)
      case "Q" | "q" => JsSuccess(Quarterly)
      case other => JsError(s"Unknown latency indicator: $other")
    }
  }

}