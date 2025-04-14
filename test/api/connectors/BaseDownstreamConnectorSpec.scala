/*
 * Copyright 2025 HM Revenue & Customs
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

package api.connectors

import api.models.outcomes.ResponseWrapper
import config.{AppConfig, MockAppConfig}
import mocks.MockHttpClient
import org.scalatest.Assertion
import support.UnitSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class BaseDownstreamConnectorSpec extends UnitSpec with MockHttpClient with MockAppConfig {
  self =>

  case class Result(value: Int)

  private val baseUrl     = "http://test-BaseUrl"
  private val path        = "some/url"
  private val absoluteUrl = s"$baseUrl/$path"
  private val body        = "body"
  private val userAgent   = "this-api"

  private implicit val correlationId: String = "someCorrelationId"
  private val outcome                        = Right(ResponseWrapper(correlationId, Result(2)))

  private val headerCarrierConfig: HeaderCarrier.Config =
    HeaderCarrier.Config(
      internalHostPatterns = List("^not-test-BaseUrl?$".r),
      headersAllowlist = Seq.empty[String],
      userAgent = Some(userAgent)
    )

  private val standardContractHeaders = Seq(
    "Authorization"       -> "auth",
    "CorrelationId"       -> correlationId,
    "OtherContractHeader" -> "OtherContractHeaderValue"
  )

  private val contentTypeHeader = "Content-Type" -> "application/json"

  private def headerCarrierForInput(inputHeaders: (String, String)*) =
    HeaderCarrier(otherHeaders = inputHeaders)

  val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
    val http: HttpClient     = mockHttpClient
    val appConfig: AppConfig = mockAppConfig
  }

  private def uri(apiContractHeaders: Seq[(String, String)] = standardContractHeaders, passThroughHeaderNames: Seq[String] = Nil) =
    new DownstreamUri[Result](
      path,
      new DownstreamStrategy {
        override def baseUrl: String = self.baseUrl

        override def contractHeaders(correlationId: String)(implicit ec: ExecutionContext): Future[Seq[(String, String)]] =
          Future.successful(apiContractHeaders)

        override def environmentHeaders: Seq[String] = passThroughHeaderNames
      })

  private implicit val httpReads: HttpReads[DownstreamOutcome[Result]] = mock[HttpReads[DownstreamOutcome[Result]]]

  "BaseDownstreamConnector" when {

    trait RequestMethodCaller {
      def makeCall(additionalRequiredHeaders: Seq[(String, String)] = Nil, additionalExcludedHeaders: Seq[(String, String)] = Nil): Assertion

      protected final def standardContractHeadersWith(additionalHeaders: Seq[(String, String)]): Seq[(String, String)] = {
        // Note: User-Agent comes from HeaderCarrier (based on HeaderCarrier.Config)
        standardContractHeaders ++ additionalHeaders :+ ("User-Agent" -> userAgent)
      }
    }

    def sendsRequest(requestMethod: RequestMethodCaller): Unit = {
      "caller makes a normal request" must {
        "make the request with the required headers and return result" in {
          requestMethod.makeCall()
        }
      }
    }

    "post is called" must {
      object Post extends RequestMethodCaller {
        def makeCall(additionalRequiredHeaders: Seq[(String, String)] = Nil, additionalExcludedHeaders: Seq[(String, String)] = Nil): Assertion = {
          implicit val hc: HeaderCarrier = headerCarrierForInput()

          MockedHttpClient.post(
            absoluteUrl,
            headerCarrierConfig,
            body,
            requiredHeaders = standardContractHeadersWith(additionalRequiredHeaders) :+ contentTypeHeader,
            excludedHeaders = additionalExcludedHeaders
          ) returns Future.successful(outcome)

          await(connector.post(body, uri())) shouldBe outcome
        }
      }

      behave like sendsRequest(Post)
    }

    "put is called" must {
      object Put extends RequestMethodCaller {
        def makeCall(additionalRequiredHeaders: Seq[(String, String)] = Nil, additionalExcludedHeaders: Seq[(String, String)] = Nil): Assertion = {
          implicit val hc: HeaderCarrier = headerCarrierForInput()

          MockedHttpClient.put(
            absoluteUrl,
            headerCarrierConfig,
            body,
            requiredHeaders = standardContractHeadersWith(additionalRequiredHeaders) :+ contentTypeHeader,
            excludedHeaders = additionalExcludedHeaders
          ) returns Future.successful(outcome)

          await(connector.put(body, uri())) shouldBe outcome
        }
      }

      behave like sendsRequest(Put)
    }

    "get is called without query parameters" must {
      object Get extends RequestMethodCaller {
        def makeCall(additionalRequiredHeaders: Seq[(String, String)] = Nil, additionalExcludedHeaders: Seq[(String, String)] = Nil): Assertion = {
          implicit val hc: HeaderCarrier = headerCarrierForInput()

          MockedHttpClient.get(
            absoluteUrl,
            headerCarrierConfig,
            requiredHeaders = standardContractHeadersWith(additionalRequiredHeaders),
            excludedHeaders = Seq(contentTypeHeader) ++ additionalExcludedHeaders
          ) returns Future.successful(outcome)

          await(connector.get(uri())) shouldBe outcome
        }
      }

      behave like sendsRequest(Get)
    }

    "get is called with query parameters" must {
      object GetWithQueryParams extends RequestMethodCaller {
        def makeCall(additionalRequiredHeaders: Seq[(String, String)] = Nil, additionalExcludedHeaders: Seq[(String, String)] = Nil): Assertion = {
          implicit val hc: HeaderCarrier = headerCarrierForInput()
          val qps: Seq[(String, String)] = List("param1" -> "value1")

          MockedHttpClient
            .get(
              absoluteUrl,
              headerCarrierConfig,
              parameters = qps,
              requiredHeaders = standardContractHeadersWith(additionalRequiredHeaders),
              excludedHeaders = Seq(contentTypeHeader) ++ additionalExcludedHeaders
            ) returns Future.successful(outcome)

          await(connector.get(uri(), queryParams = qps)) shouldBe outcome
        }
      }

      behave like sendsRequest(GetWithQueryParams)
    }

    "delete is called" must {
      object Delete extends RequestMethodCaller {
        def makeCall(additionalRequiredHeaders: Seq[(String, String)] = Nil, additionalExcludedHeaders: Seq[(String, String)] = Nil): Assertion = {
          implicit val hc: HeaderCarrier = headerCarrierForInput()

          MockedHttpClient.delete(
            absoluteUrl,
            headerCarrierConfig,
            requiredHeaders = standardContractHeadersWith(additionalRequiredHeaders),
            excludedHeaders = Seq(contentTypeHeader) ++ additionalExcludedHeaders
          ) returns Future.successful(outcome)

          await(connector.delete(uri())) shouldBe outcome
        }
      }

      behave like sendsRequest(Delete)
    }

    "a request is received with headers" must {
      def makeCall(downstreamUri: DownstreamUri[Result], requiredHeaders: Seq[(String, String)] = Nil, excludedHeaders: Seq[(String, String)] = Nil)(
          implicit hc: HeaderCarrier): Assertion = {
        MockedHttpClient.put(
          absoluteUrl,
          headerCarrierConfig,
          body,
          requiredHeaders = requiredHeaders,
          excludedHeaders = excludedHeaders
        ) returns Future.successful(outcome)

        await(connector.put(body, downstreamUri)) shouldBe outcome
      }

      "not pass through input headers that aren't in environmentHeaders" in {
        val inputHeader                = "Header1" -> "Value1"
        implicit val hc: HeaderCarrier = headerCarrierForInput(inputHeader)

        makeCall(uri(), excludedHeaders = Seq(inputHeader))
      }

      "pass through input headers that are in environmentHeaders" when {
        def passThroughInputs(passThrough: String): Assertion = {
          val inputHeader                = "Header1" -> "Value1"
          implicit val hc: HeaderCarrier = headerCarrierForInput(inputHeader)

          makeCall(uri(passThroughHeaderNames = Seq(passThrough)), requiredHeaders = Seq("Header1" -> "Value1"))
        }

        "configured environment header name and input header name match exactly" in {
          behave like passThroughInputs("Header1")
        }

        "configured environment header name and input header name differ by case" in {
          behave like passThroughInputs("header1")
        }
      }

      "contract headers must override input headers (even if configured for pass through)" when {
        def contractHeadersOverrideInputHeaders(inputHeader: (String, String)): Assertion = {
          val contractHeader = "Header1" -> "ContractValue"

          implicit val hc: HeaderCarrier = headerCarrierForInput(inputHeader)

          makeCall(
            uri(apiContractHeaders = Seq(contractHeader), passThroughHeaderNames = Seq("Header1")),
            requiredHeaders = Seq(contractHeader),
            excludedHeaders = Seq(inputHeader)
          )
        }

        "input header name matches exactly" in {
          contractHeadersOverrideInputHeaders("Header1" -> "InputValue")
        }

        "input header name has different case" in {
          contractHeadersOverrideInputHeaders("header1" -> "InputValue")
        }
      }

      "automatically added additional headers (typically content-type) must override input values (even if configured for pass through)" when {
        def additionalHeadersOverrideInputHeaders(inputHeader: (String, String)): Assertion = {
          implicit val hc: HeaderCarrier = headerCarrierForInput(inputHeader)

          makeCall(
            uri(apiContractHeaders = Nil, passThroughHeaderNames = Seq("Content-Type")),
            requiredHeaders = Seq(contentTypeHeader),
            excludedHeaders = Seq(inputHeader)
          )
        }

        "input header name matches exactly" in {
          behave like additionalHeadersOverrideInputHeaders("Content-Type" -> "application/user-type")
        }

        "input header name has different case" in {
          behave like additionalHeadersOverrideInputHeaders("content-type" -> "application/user-type")
        }
      }
    }
  }

}
