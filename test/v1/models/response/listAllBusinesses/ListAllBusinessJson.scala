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

package v1.models.response.listAllBusinesses

object ListAllBusinessJson {

  val ifsResponseWithBusinessData: String =
    """
    {
    "processingDate": "2023-07-05T09:16:58.655Z",
    "taxPayerDisplayResponse": {
     "safeId": "XE00001234567890",
     "nino": "AA123456A",
     "mtdbsa": "123456789012345",
     "propertyIncome": false,
     "businessData": [
        {
           "incomeSourceType": "doesn't matter",
           "incomeSourceId": "123456789012345",
           "accountingPeriodStartDate": "2001-01-01",
           "accountingPeriodEndDate": "2001-01-01",
           "tradingName": "RCDTS",
           "businessAddressDetails": {
              "addressLine1": "100 SuttonStreet",
              "addressLine2": "Wokingham",
              "addressLine3": "Surrey",
              "addressLine4": "London",
              "postalCode": "DH14EJ",
              "countryCode": "GB"
           },
           "businessContactDetails": {
              "phoneNumber": "01332752856",
              "mobileNumber": "07782565326",
              "faxNumber": "01332754256",
              "emailAddress": "stephen@manncorpone.co.uk"
           },
           "tradingStartDate": "2001-01-01",
           "cashOrAccruals": "cash",
           "seasonal": true,
           "cessationDate": "2001-01-01",
           "cessationReason": "002",
           "paperLess": true
        },
        {
           "incomeSourceType": "doesn't matter",
           "incomeSourceId": "098765432109876",
           "accountingPeriodStartDate": "2001-01-01",
           "accountingPeriodEndDate": "2001-01-01",
           "tradingName": "RCDTS 2",
           "businessAddressDetails": {
              "addressLine1": "100 SuttonStreet",
              "addressLine2": "Wokingham",
              "addressLine3": "Surrey",
              "addressLine4": "London",
              "postalCode": "DH14EJ",
              "countryCode": "GB"
           },
           "businessContactDetails": {
              "phoneNumber": "01332752856",
              "mobileNumber": "07782565326",
              "faxNumber": "01332754256",
              "emailAddress": "stephen@manncorpone.co.uk"
           },
           "tradingStartDate": "2001-01-01",
           "cashOrAccruals": "cash",
           "seasonal": true,
           "cessationDate": "2001-01-01",
           "cessationReason": "002",
           "paperLess": true
        }
       ]
      }
    }
    """

  val ifsResponseWithPropertyData =
    """
  {
    "processingDate": "2023-07-05T09:16:58.655Z",
    "taxPayerDisplayResponse": {
      "safeId": "XE00001234567890",
      "nino": "AA123456A",
      "mtdbsa": "123456789012345",
      "propertyIncome": true,
      "propertyData": [
        {
          "incomeSourceType": "uk-property",
          "incomeSourceId": "123456789012345",
          "accountingPeriodStartDate": "2001-01-01",
          "accountingPeriodEndDate": "2001-01-01",
          "tradingStartDate": "2001-01-01",
          "cashOrAccrualsFlag": true,
          "numPropRented": 0,
          "numPropRentedUK": 0,
          "numPropRentedEEA": 5,
          "numPropRentedNONEEA": 1,
          "emailAddress": "stephen@manncorpone.co.uk",
          "cessationDate": "2001-01-01",
          "cessationReason": "002",
          "paperLess": true,
          "incomeSourceStartDate": "2019-07-14"
        },
        {
          "incomeSourceType": "foreign-property",
          "incomeSourceId": "098765432109876",
          "accountingPeriodStartDate": "2001-01-01",
          "accountingPeriodEndDate": "2001-01-01",
          "tradingStartDate": "2001-01-01",
          "cashOrAccrualsFlag": true,
          "numPropRented": 0,
          "numPropRentedUK": 0,
          "numPropRentedEEA": 5,
          "numPropRentedNONEEA": 1,
          "emailAddress": "stephen@manncorpone.co.uk",
          "cessationDate": "2001-01-01",
          "cessationReason": "002",
          "paperLess": true,
          "incomeSourceStartDate": "2019-07-14"
        }
      ]
    }
  }
  """

  val ifsResponseWithPropertyAndBusinessData =
    """
    {
    "processingDate": "2023-07-05T09:16:58.655Z",
      "taxPayerDisplayResponse": {
        "safeId": "XE00001234567890",
        "nino": "AA123456A",
        "mtdbsa": "123456789012345",
        "propertyIncome": true,
        "businessData": [
          {
            "incomeSourceType": "doesn't matter",
            "incomeSourceId": "123456789012345",
            "accountingPeriodStartDate": "2001-01-01",
            "accountingPeriodEndDate": "2001-01-01",
            "tradingName": "RCDTS",
            "businessAddressDetails": {
              "addressLine1": "100 SuttonStreet",
              "addressLine2": "Wokingham",
              "addressLine3": "Surrey",
              "addressLine4": "London",
              "postalCode": "DH14EJ",
              "countryCode": "GB"
            },
            "businessContactDetails": {
              "phoneNumber": "01332752856",
              "mobileNumber": "07782565326",
              "faxNumber": "01332754256",
              "emailAddress": "stephen@manncorpone.co.uk"
            },
            "tradingStartDate": "2001-01-01",
            "cashOrAccruals": "cash",
            "seasonal": true,
            "cessationDate": "2001-01-01",
            "cessationReason": "002",
            "paperLess": true
          },
          {
            "incomeSourceType": "doesn't matter",
            "incomeSourceId": "098765432109876",
            "accountingPeriodStartDate": "2001-01-01",
            "accountingPeriodEndDate": "2001-01-01",
            "tradingName": "RCDTS 2",
            "businessAddressDetails": {
              "addressLine1": "100 SuttonStreet",
              "addressLine2": "Wokingham",
              "addressLine3": "Surrey",
              "addressLine4": "London",
              "postalCode": "DH14EJ",
              "countryCode": "GB"
            },
            "businessContactDetails": {
              "phoneNumber": "01332752856",
              "mobileNumber": "07782565326",
              "faxNumber": "01332754256",
              "emailAddress": "stephen@manncorpone.co.uk"
            },
            "tradingStartDate": "2001-01-01",
            "cashOrAccruals": "cash",
            "seasonal": true,
            "cessationDate": "2001-01-01",
            "cessationReason": "002",
            "paperLess": true
          }
        ],
        "propertyData": [
          {
            "incomeSourceType": "uk-property",
            "incomeSourceId": "123456789012345",
            "accountingPeriodStartDate": "2001-01-01",
            "accountingPeriodEndDate": "2001-01-01",
            "tradingStartDate": "2001-01-01",
            "cashOrAccrualsFlag": true,
            "numPropRented": 0,
            "numPropRentedUK": 0,
            "numPropRentedEEA": 5,
            "numPropRentedNONEEA": 1,
            "emailAddress": "stephen@manncorpone.co.uk",
            "cessationDate": "2001-01-01",
            "cessationReason": "002",
            "paperLess": true,
            "incomeSourceStartDate": "2019-07-14"
          },
          {
            "incomeSourceType": "foreign-property",
            "incomeSourceId": "098765432109876",
            "accountingPeriodStartDate": "2001-01-01",
            "accountingPeriodEndDate": "2001-01-01",
            "tradingStartDate": "2001-01-01",
            "cashOrAccrualsFlag": true,
            "numPropRented": 0,
            "numPropRentedUK": 0,
            "numPropRentedEEA": 5,
            "numPropRentedNONEEA": 1,
            "emailAddress": "stephen@manncorpone.co.uk",
            "cessationDate": "2001-01-01",
            "cessationReason": "002",
            "paperLess": true,
            "incomeSourceStartDate": "2019-07-14"
          }
        ]
      }
    }
    """

  val desResponseWithBusinessData: String =
    """
    {
     "safeId": "XE00001234567890",
     "nino": "AA123456A",
     "mtdbsa": "123456789012345",
     "propertyIncome": false,
     "businessData": [
       {
          "incomeSourceType": "doesn't matter",
          "incomeSourceId": "123456789012345",
          "accountingPeriodStartDate": "2001-01-01",
          "accountingPeriodEndDate": "2001-01-01",
          "tradingName": "RCDTS",
          "businessAddressDetails": {
             "addressLine1": "100 SuttonStreet",
             "addressLine2": "Wokingham",
             "addressLine3": "Surrey",
             "addressLine4": "London",
             "postalCode": "DH14EJ",
             "countryCode": "GB"
          },
          "businessContactDetails": {
             "phoneNumber": "01332752856",
             "mobileNumber": "07782565326",
             "faxNumber": "01332754256",
             "emailAddress": "stephen@manncorpone.co.uk"
          },
          "tradingStartDate": "2001-01-01",
          "cashOrAccruals": "cash",
          "seasonal": true,
          "cessationDate": "2001-01-01",
          "cessationReason": "002",
          "paperLess": true
       },
       {
          "incomeSourceType": "doesn't matter",
          "incomeSourceId": "098765432109876",
          "accountingPeriodStartDate": "2001-01-01",
          "accountingPeriodEndDate": "2001-01-01",
          "tradingName": "RCDTS 2",
          "businessAddressDetails": {
             "addressLine1": "100 SuttonStreet",
             "addressLine2": "Wokingham",
             "addressLine3": "Surrey",
             "addressLine4": "London",
             "postalCode": "DH14EJ",
             "countryCode": "GB"
          },
          "businessContactDetails": {
             "phoneNumber": "01332752856",
             "mobileNumber": "07782565326",
             "faxNumber": "01332754256",
             "emailAddress": "stephen@manncorpone.co.uk"
          },
          "tradingStartDate": "2001-01-01",
          "cashOrAccruals": "cash",
          "seasonal": true,
          "cessationDate": "2001-01-01",
          "cessationReason": "002",
          "paperLess": true
       }
       ]
    }
    """

  val desResponseWithPropertyData =
    """
  {
    "safeId": "XE00001234567890",
    "nino": "AA123456A",
    "mtdbsa": "123456789012345",
    "propertyIncome": true,
    "propertyData": [
      {
        "incomeSourceType": "uk-property",
        "incomeSourceId": "123456789012345",
        "accountingPeriodStartDate": "2001-01-01",
        "accountingPeriodEndDate": "2001-01-01",
        "tradingStartDate": "2001-01-01",
        "cashOrAccrualsFlag": true,
        "numPropRented": 0,
        "numPropRentedUK": 0,
        "numPropRentedEEA": 5,
        "numPropRentedNONEEA": 1,
        "emailAddress": "stephen@manncorpone.co.uk",
        "cessationDate": "2001-01-01",
        "cessationReason": "002",
        "paperLess": true,
        "incomeSourceStartDate": "2019-07-14"
      },
      {
        "incomeSourceType": "foreign-property",
        "incomeSourceId": "098765432109876",
        "accountingPeriodStartDate": "2001-01-01",
        "accountingPeriodEndDate": "2001-01-01",
        "tradingStartDate": "2001-01-01",
        "cashOrAccrualsFlag": true,
        "numPropRented": 0,
        "numPropRentedUK": 0,
        "numPropRentedEEA": 5,
        "numPropRentedNONEEA": 1,
        "emailAddress": "stephen@manncorpone.co.uk",
        "cessationDate": "2001-01-01",
        "cessationReason": "002",
        "paperLess": true,
        "incomeSourceStartDate": "2019-07-14"
      }
    ]
  }
  """

  val desResponseWithPropertyAndBusinessData =
    """
    {
      "safeId": "XE00001234567890",
      "nino": "AA123456A",
      "mtdbsa": "123456789012345",
      "propertyIncome": true,
      "businessData": [
        {
          "incomeSourceType": "doesn't matter",
          "incomeSourceId": "123456789012345",
          "accountingPeriodStartDate": "2001-01-01",
          "accountingPeriodEndDate": "2001-01-01",
          "tradingName": "RCDTS",
          "businessAddressDetails": {
            "addressLine1": "100 SuttonStreet",
            "addressLine2": "Wokingham",
            "addressLine3": "Surrey",
            "addressLine4": "London",
            "postalCode": "DH14EJ",
            "countryCode": "GB"
          },
          "businessContactDetails": {
            "phoneNumber": "01332752856",
            "mobileNumber": "07782565326",
            "faxNumber": "01332754256",
            "emailAddress": "stephen@manncorpone.co.uk"
          },
          "tradingStartDate": "2001-01-01",
          "cashOrAccruals": "cash",
          "seasonal": true,
          "cessationDate": "2001-01-01",
          "cessationReason": "002",
          "paperLess": true
        },
        {
          "incomeSourceType": "doesn't matter",
          "incomeSourceId": "098765432109876",
          "accountingPeriodStartDate": "2001-01-01",
          "accountingPeriodEndDate": "2001-01-01",
          "tradingName": "RCDTS 2",
          "businessAddressDetails": {
            "addressLine1": "100 SuttonStreet",
            "addressLine2": "Wokingham",
            "addressLine3": "Surrey",
            "addressLine4": "London",
            "postalCode": "DH14EJ",
            "countryCode": "GB"
          },
          "businessContactDetails": {
            "phoneNumber": "01332752856",
            "mobileNumber": "07782565326",
            "faxNumber": "01332754256",
            "emailAddress": "stephen@manncorpone.co.uk"
          },
          "tradingStartDate": "2001-01-01",
          "cashOrAccruals": "cash",
          "seasonal": true,
          "cessationDate": "2001-01-01",
          "cessationReason": "002",
          "paperLess": true
        }
      ],
      "propertyData": [
        {
          "incomeSourceType": "uk-property",
          "incomeSourceId": "123456789012345",
          "accountingPeriodStartDate": "2001-01-01",
          "accountingPeriodEndDate": "2001-01-01",
          "tradingStartDate": "2001-01-01",
          "cashOrAccrualsFlag": true,
          "numPropRented": 0,
          "numPropRentedUK": 0,
          "numPropRentedEEA": 5,
          "numPropRentedNONEEA": 1,
          "emailAddress": "stephen@manncorpone.co.uk",
          "cessationDate": "2001-01-01",
          "cessationReason": "002",
          "paperLess": true,
          "incomeSourceStartDate": "2019-07-14"
        },
        {
          "incomeSourceType": "foreign-property",
          "incomeSourceId": "098765432109876",
          "accountingPeriodStartDate": "2001-01-01",
          "accountingPeriodEndDate": "2001-01-01",
          "tradingStartDate": "2001-01-01",
          "cashOrAccrualsFlag": true,
          "numPropRented": 0,
          "numPropRentedUK": 0,
          "numPropRentedEEA": 5,
          "numPropRentedNONEEA": 1,
          "emailAddress": "stephen@manncorpone.co.uk",
          "cessationDate": "2001-01-01",
          "cessationReason": "002",
          "paperLess": true,
          "incomeSourceStartDate": "2019-07-14"
        }
      ]
    }
    """

}
