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

package api.models.errors

import play.api.http.Status.*

// Format Errors
object NinoFormatError       extends MtdError("FORMAT_NINO", "The provided NINO is invalid", BAD_REQUEST)
object TaxYearFormatError    extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid", BAD_REQUEST)
object DateFormatError       extends MtdError(code = "FORMAT_DATE", message = "The supplied date format is not valid", BAD_REQUEST)
object BusinessIdFormatError extends MtdError("FORMAT_BUSINESS_ID", "The provided Business ID is invalid", BAD_REQUEST)

object ValueFormatError extends MtdError("FORMAT_VALUE", "The value must be between 0 and 99999999999.99", BAD_REQUEST) {

  def forPathAndRange(path: String, min: String, max: String): MtdError =
    ValueFormatError.copy(paths = Some(Seq(path)), message = s"The value must be between $min and $max")

}

object StartDateFormatError extends MtdError("FORMAT_START_DATE", "The start date field format is invalid", BAD_REQUEST)
object EndDateFormatError   extends MtdError("FORMAT_END_DATE", "The end date field format is invalid", BAD_REQUEST)

object NoBusinessFoundError extends MtdError("NO_BUSINESS_FOUND", "No business found for given NINO and Business ID", NOT_FOUND)

// Rule Errors
object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleTaxYearNotEndedError extends MtdError("RULE_TAX_YEAR_NOT_ENDED", "The specified tax year has not yet ended", BAD_REQUEST)

object RuleTaxYearRangeInvalidError extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "A tax year range of one year is required", BAD_REQUEST)

//Standard Errors
object NotFoundError          extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found", NOT_FOUND)
object InternalError          extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred", INTERNAL_SERVER_ERROR)
object BadRequestError        extends MtdError("INVALID_REQUEST", "Invalid request", BAD_REQUEST)
object GatewayTimeoutError    extends MtdError("GATEWAY_TIMEOUT", "The request has timed out", GATEWAY_TIMEOUT)
object BVRError               extends MtdError("BUSINESS_ERROR", "Business validation error", BAD_REQUEST)
object InvalidHttpMethodError extends MtdError("INVALID_HTTP_METHOD", "Invalid HTTP method", METHOD_NOT_ALLOWED)

//Authorisation Errors

object ClientOrAgentNotAuthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client or agent is not authorised", FORBIDDEN) {
  def withStatus401: MtdError = copy(httpStatus = UNAUTHORIZED)
}

object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid", NOT_ACCEPTABLE)
object UnsupportedVersionError  extends MtdError("NOT_FOUND", "The requested resource could not be found", NOT_FOUND)
object InvalidBodyTypeError     extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)

//Stub Errors
object RuleIncorrectGovTestScenarioError
    extends MtdError("RULE_INCORRECT_GOV_TEST_SCENARIO", "The supplied Gov-Test-Scenario is not valid", BAD_REQUEST)

object RuleRequestCannotBeFulfilledError
    extends MtdError("RULE_REQUEST_CANNOT_BE_FULFILLED", "Custom (will vary in production depending on the actual error)", UNPROCESSABLE_ENTITY) {
  def incomeSourceTypeMsg: MtdError = RuleRequestCannotBeFulfilledError.copy(message = "Request cannot proceed for this income source type")
}
