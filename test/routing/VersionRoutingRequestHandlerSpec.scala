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

package routing

import api.models.errors.{InvalidAcceptHeaderError, MtdError, UnsupportedVersionError}
import config.MockAppConfig
import org.apache.pekko.actor.ActorSystem
import org.scalatest.Inside
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HeaderNames.ACCEPT
import play.api.http.{HttpConfiguration, HttpErrorHandler, HttpFilters}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.routing.Router
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.UnitSpec

class VersionRoutingRequestHandlerSpec extends UnitSpec with Inside with MockAppConfig with GuiceOneAppPerSuite {

  implicit private val actorSystem: ActorSystem = ActorSystem("test")
  val action: DefaultActionBuilder              = app.injector.instanceOf[DefaultActionBuilder]

  import play.api.mvc.Handler
  import play.api.routing.sird._

  case object DefaultHandler extends Handler

  case object V1Handler extends Handler

  case object V2Handler extends Handler

  private val version1_enabled  = Version("1.0")
  private val version2_disabled = Version("2.0")

  private val routingMap = new VersionRoutingMap {
    override val defaultRouter: Router = Router.from { case GET(p"/docs") => DefaultHandler }

    override val map: Map[Version, Router] = Map(
      version1_enabled  -> Router.from { case GET(p"/v1") => V1Handler },
      version2_disabled -> Router.from { case GET(p"/v2") => V2Handler }
    )

  }

  class Test {
    MockedAppConfig.endpointsEnabled(version1_enabled).returns(true).anyNumberOfTimes()
    MockedAppConfig.endpointsEnabled(version2_disabled).returns(false).anyNumberOfTimes()

    val httpConfiguration: HttpConfiguration = HttpConfiguration("context")
    private val errorHandler                 = mock[HttpErrorHandler]
    private val filters                      = mock[HttpFilters]
    (() => filters.filters).stubs().returns(Seq.empty)

    val requestHandler: VersionRoutingRequestHandler =
      new VersionRoutingRequestHandler(routingMap, errorHandler, httpConfiguration, mockAppConfig, filters, action)

    def buildRequest(path: String)(implicit acceptHeader: Option[String]): RequestHeader =
      acceptHeader.foldLeft(FakeRequest("GET", path)) { (req, accept) => req.withHeaders((ACCEPT, accept)) }

  }

  private def returnHandler(path: String, maybeExpectedHandler: Option[Handler])(implicit acceptHeader: Option[String]): Unit =
    s"return handler $maybeExpectedHandler" in new Test {
      requestHandler.routeRequest(buildRequest(path)) shouldBe maybeExpectedHandler
    }

  private def returnHandlerIgnoringTrailingSlash(path: String, expectedHandler: Handler)(implicit acceptHeader: Option[String]): Unit = {
    "matches exactly" must {
      returnHandler(path, Some(expectedHandler))
    }

    "matches except for a trailing slash" must {
      returnHandler(s"$path/", Some(expectedHandler))
    }
  }

  private def returnHandlerThatRespondsWithError(path: String = "/ignored", mtdError: MtdError)(implicit acceptHeader: Option[String]): Unit =
    s"return a handler that responds with $mtdError" in new Test {
      val request: RequestHeader = buildRequest(path)

      inside(requestHandler.routeRequest(request)) { case Some(action: EssentialAction) =>
        val result = action.apply(request)

        status(result) shouldBe mtdError.httpStatus
        contentAsJson(result) shouldBe Json.toJson(mtdError)
      }

    }

  "Routing requests" when {
    "no version is in the accept header" when {
      implicit val acceptHeader: Option[String] = None

      "the path matches a handler in the documentation (default) router" must {
        returnHandlerIgnoringTrailingSlash("/docs", DefaultHandler)
      }

      "the path does not match a handler in the documentation (default) router" must {
        "expect a versioned accept header and return a handler that responds with 406 (InvalidAcceptHeaderError)" must {
          returnHandlerThatRespondsWithError(path = "/unmatched", mtdError = InvalidAcceptHeaderError)
        }
      }
    }

    "a well-formed and enabled version string is in the accept header" when {
      implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.1.0+json")

      "the path matches a handler in the documentation (default) router" must {
        returnHandlerIgnoringTrailingSlash("/docs", DefaultHandler)
      }

      "the path does not match a handler in the documentation (default) router" when {
        "the path matches a handler for the versioned API router" must {
          returnHandlerIgnoringTrailingSlash("/v1", V1Handler)
        }

        "the path does not match a handler for the versioned API router" must {
          returnHandler("/other", None)
        }
      }
    }

    "a well-formed but unknown version string is in the accept header" must {
      implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.5.0+json")

      "return a handler that responds with 404 (UnsupportedVersionError)" must {
        returnHandlerThatRespondsWithError(mtdError = UnsupportedVersionError)
      }
    }

    "a well-formed but disabled version string is in the accept header" must {
      implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.2.0+json")

      "return a handler that responds with 404 (UnsupportedVersionError)" must {
        returnHandlerThatRespondsWithError(mtdError = UnsupportedVersionError)
      }
    }

    "a malformed version string is in the accept header" must {
      implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.XXXXX+json")

      "return a handler that responds with 406 (InvalidAcceptHeaderError)" must {
        returnHandlerThatRespondsWithError(mtdError = InvalidAcceptHeaderError)
      }
    }
  }

}
