package v2.createUpdatePeriodsOfAccount

import api.connectors.DownstreamUri.HipUri
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.createUpdatePeriodsOfAccount.request.CreateUpdatePeriodsOfAccountRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateUpdatePeriodsOfAccountConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def create(request: CreateUpdatePeriodsOfAccountRequestData)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    implicit val succesCode: SuccessCode = SuccessCode(NO_CONTENT)

    val downstreamUri = HipUri[Unit](s"itsd/income-sources/$nino/periods-of-account/$businessId?taxYear=${taxYear.asTysDownstream}")

    put(body, downstreamUri)
  }
}
