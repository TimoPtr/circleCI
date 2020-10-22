/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

@file:Suppress("unused")

package com.kolibree.android.network.api

import androidx.annotation.Keep

/** [ApiError] codes definitions */
/*
Source: https://github.com/kolibree-git/production-core/blob/beta/kolibree_core_API/errors.py#L37
Last updated: 2019, August 2nd (creation)
 */
@SuppressWarnings("LargeClass")
@Keep
object ApiErrorCode {

    /**
     * SDK2: Unknown error, please report.
     *
     * This error code is local to the SDK and is not returned by the backend.
     */
    const val UNKNOWN_ERROR = -2

    /**
     * SDK1: Network error.
     *
     * This error code is local to the SDK and is not returned by the backend.
     */
    const val NETWORK_ERROR = -1

    /**
     * E01: Need valid client credentials.
     */
    const val MISSING_CLIENT_ID_OR_SIG = 1

    /**
     * E02: Need valid client credentials.
     */
    const val CLIENT_DOES_NOT_EXIST_1 = 2

    /**
     * E03: Need valid client credentials.
     */
    const val CLIENT_WRONG_SIGNATURE = 3

    /**
     * E04: Account does not exist.
     */
    const val ACCOUNT_DOES_NOT_EXIST = 4

    /**
     * E05: Invalid access token.
     */
    const val INVALID_ACCESS_TOKEN = 5

    /**
     * E06: Invalid access token.
     */
    const val ACCESS_TOKEN_HAS_EXPIRED = 6

    /**
     * E07: Profile is not associated with this account.
     */
    const val PROFILE_WRONG_ASSOCIATION = 7

    /**
     * E08: Account: wrong data. Email is missing.
     */
    const val ACCOUNT_EMAIL_MISSING = 8

    /**
     * E09: Account: wrong data. Account type is missing.
     */
    const val ACCOUNT_TYPE_MISSING = 9

    /**
     * E10: Account: wrong data. Password type is missing.
     */
    const val PASSWORD_TYPE_MISSING = 10

    /**
     * E11: Account: wrong data. Missing valid account credientials, either email + password or facebook_id +
     * facebook_auth_token
     */
    const val ACCOUNT_INVALID_CREDENTIALS = 11

    /**
     * E12: Account: wrong data. Invalid Facebook token.
     */
    const val INVALID_FACEBOOK_TOKEN = 12

    /**
     * E13: Account: wrong data. google_id is missing.
     */
    const val MISSING_GOOGLE_ID = 13

    /**
     * E14: Profile: wrong data. first_name is missing.
     */
    const val PROFILE_MISSING_FIRST_NAME = 14

    /**
     * E15: Profile: wrong data. last_name is missing.
     */
    const val PROFILE_MISSING_LAST_NAME = 15

    /**
     * E16: Profile: wrong data. gender is missing.
     */
    const val PROFILE_MISSING_GENDER = 16

    /**
     * E17: Profile: wrong data. birthday is missing.
     */
    const val PROFILE_MISSING_BIRTHDAY = 17

    /**
     * E18: Account: wrong data. country is missing.
     */
    const val ACCOUNT_MISSING_COUNTRY = 18

    /**
     * E19: Account: wrong data.
     */
    const val ACCOUNT_WRONG_DATA = 19

    /**
     * E20: Profile: wrong data.
     */
    const val PROFILE_WRONG_DATA = 20

    /**
     * E21: Unable to create profile in User API.
     */
    const val UNABLE_TO_CREATE_USER_IN_USER_API = 21

    /**
     * E22: Account creation error.
     */
    const val ACCOUNT_CREATION_ERROR = 22

    /**
     * E23: Account update error.
     */
    const val ACCOUNT_UPDATE_ERROR = 23

    /**
     * E24: Invalid refresh token.
     */
    const val INVALID_REFRESH_TOKEN = 24

    /**
     * E25: Missing or wrong password.
     */
    const val MISSING_OR_WRONG_PASSWORD = 25

    /**
     * E26: Profile does not exist in user API.
     */
    const val PROFILE_DOES_NOT_EXIST_IN_USER_API = 26

    /**
     * E27: Unable to update profile on user API.
     */
    const val UNABLE_TO_UPDATE_PROFILE_USER_API = 27

    /**
     * E28: Unable to delete profile.
     */
    const val UNABLE_TO_DELETE_PROFILE = 28

    /**
     * E33: Reset password token has expired.
     */
    const val RESET_PASSWORD_TOKEN_EXPIRED = 33

    /**
     * E34: Brushing is not associated to this profile.
     */
    const val WRONG_BRUSHING_PROFILE_ASSOCATION = 34

    /**
     * E35: Brushing: wrong data. duration is missing.
     */
    const val BRUSHING_MISSING_DURATION = 35

    /**
     * E36: Brushing: wrong data. goal_duration is missing.
     */
    const val BRUSHING_MISSING_GOAL_DURATION = 36

    /**
     * E37: Brushing: wrong data. datetime is missing
     */
    const val BRUSHING_DATETIME_MISSING = 38

    /**
     * E39: Account: wrong data. The account does not exist and additional information is needed to create it.
     */
    const val ACCOUNT_ADDITIONAL_INFO_NEEDED = 39

    /**
     * E40: Invalid permission. The requester account is not allowed to access the requested data.
     */
    const val INVALID_PERMISSION = 40

    /**
     * E41: picture_get_url missing.
     */
    const val PICTURE_GET_URL_MISSING = 41

    /**
     * E42: picture_get_url wrong value.
     */
    const val PICTURE_GET_URL_WRONG_VALUE = 42

    /**
     * E43: Cannot delete owner profile of an account.
     */
    const val CANT_DELETE_OWNER_PROFILE = 43

    /**
     * E44: Your client is not authorized to access this API version. Please use /dentegra/ instead of /v1/.
     */
    const val CLIENT_WRONG_API_1 = 44

    /**
     * E45: Your client is not authorized to access this API version. Please use /v1/ instead of /dentegra/.
     */
    const val CLIENT_WRONG_API_2 = 45

    /**
     * E46: You have not sufficient rights to access this ressource.
     */
    const val UNSUFFICIENT_RIGHTS = 46

    /**
     * E47: Stat does not exist.
     */
    const val STAT_DOES_NOT_EXIST = 47

    /**
     * E48: Badge does not exist.
     */
    const val BADGE_DOES_NOT_EXIST = 48

    /**
     * E49: Account level permission is false. Can't modify profile permissions. Link the account with dentegra first.
     */
    const val ACCOUNT_LEVEL_PERMISSION_FALSE = 49

    /**
     * E50: Missing dentegra_account_id.
     */
    const val MISSING_DENTEGRA_ACCOUNT_ID = 50

    /**
     * E51: Brushing file does not exist.
     */
    const val BRUSHING_FILE_DOES_NOT_EXIST = 51

    /**
     * E52: No information for this device.
     */
    const val NO_MODIFICATION_HISTORY = 52

    /**
     * E53: This country is not supported.
     */
    const val UNSUPPORTED_COUNTRY = 53

    /**
     * E54: Missing order_id.
     */
    const val MISSING_ORDER_ID = 54

    /**
     * E55: Order does not exist.
     */
    const val ORDER_DOES_NOT_EXIST = 55

    /**
     * E56: Wrong order status. Cannot modify this order because of his order status.
     */
    const val WRONG_ORDER_STATUS = 56

    /**
     * E57: Dentegra account doesn't exist.
     */
    const val DENTEGRA_ACCOUNT_DOES_NOT_EXIST = 57

    /**
     * E58: Dentegra profile does not exist.
     */
    const val DENTEGRA_PROFILE_DOES_NOT_EXIST = 58

    /**
     * E59: Missing 'token' query argument.
     */
    const val MISSING_TOKEN_Q_ARG = 59

    /**
     * E60: Wrong 'token' format.
     */
    const val WRONG_TOKEN_FORMAT = 60

    /**
     * E61: Invalid token. No user was found for this token
     */
    const val NO_USER_TOKEN = 61

    /**
     * E62: Token expired.
     */
    const val TOKEN_HAS_EXPIRED = 62

    /**
     * E63: Shipping method does not exist.
     */
    const val SHIPPING_METHOD_DOES_NOT_EXIST = 63

    /**
     * E64: Invalid shipping method.
     */
    const val INACTIVE_SHIPPING_METHOD = 64

    /**
     * E65: No password for this account. Maybe you should login with Facebook.
     */
    const val ACCOUNT_WITHOUT_PASSWORD = 65

    /**
     * E66: An account with the same dentegra_account_id already exists.
     */
    const val DUPLICATE_DENTEGRA_ACCOUNT_ID = 66

    /**
     * E67: A profile with the same dentegra_profile_id already exists.
     */
    const val DUPLICATE_DENTEGRA_PROFILE_ID_1 = 67

    /**
     * E68: A profile with the same dentegra_profile_id already exists.
     */
    const val DUPLICATE_DENTEGRA_PROFILE_ID_2 = 68

    /**
     * E69: A profile with the same dentegra_profile_id already exists.
     */
    const val DUPLICATE_DENTEGRA_PROFILE_ID_3 = 69

    /**
     * E70: Wrong arguments.
     */
    const val WRONG_ARGUMENTS = 70

    /**
     * E71: An account with the same dentegra_account_id already exists.
     */
    const val DUPLICATE_DENTEGRA_ACCOUNT_ID_2 = 71

    /**
     * E72: Invalid custom payment. Custom payment does not exist or is already paid.
     */
    const val INVALID_CUSTOM_PAYMENT = 72

    /**
     * E73: Profile does not exist
     */
    const val PROFILE_DOES_NOT_EXIST_BIS = 73

    /**
     * E75: Invalid access token.
     */
    const val PRACTITIONER_MISSING_ACCESS_TOKEN = 75

    /**
     * E76: Invalid access token.
     */
    const val PRACTITIONER_INVALID_ACCESS_TOKEN = 76

    /**
     * E77: Invalid access token.
     */
    const val PRACTITIONER_EXPIRED_ACCESS_TOKEN = 77

    /**
     * E78: Practitioner: wrong data. Email is missing.
     */
    const val PRACTITIONER_MISSING_EMAIL = 78

    /**
     * E79: Practitioner: wrong data. Password is missing.
     */
    const val PRACTITIONER_MISSING_PASSWORD = 79

    /**
     * E80: Practitioner: wrong data. First name is missing.
     */
    const val PRACTITIONER_MISSING_FIRST_NAME = 80

    /**
     * E81: Practitioner: wrong data. Last name is missing.
     */
    const val PRACTITIONER_MISSING_LAST_NAME = 81

    /**
     * E82: Practitioner: wrong data. Gender is missing.
     */
    const val PRACTITIONER_MISSING_GENDER = 82

    /**
     * E83: Practitioner: wrong data. Birth date is missing.
     */
    const val PRACTITIONER_MISSING_BIRTH_DATE = 83

    /**
     * E84: Practitioner: wrong data. Job is missing.
     */
    const val PRACTITIONER_MISSING_JOB = 84

    /**
     * E85: Practitioner: wrong data. Speciality is missing.
     */
    const val PRACTITIONER_MISSING_SPECIALITY = 85

    /**
     * E86: Practitioner: wrong data. Password doesn't meet complexity requirements.
     */
    const val PRACTITIONER_PASSWORD_COMPLEXITY_NOT_MET = 86

    /**
     * E87: Practitioner: wrong data.
     */
    const val PRACTITIONER_WRONG_DATA = 87

    /**
     * E88: Practitioner does not exist.
     */
    const val PRACTITIONER_DOESNT_EXIST = 88

    /**
     * E89: Practitioner: invalid email or password.
     */
    const val PRACTITIONER_INVALID_EMAIL_OR_PASSWORD = 89

    /**
     * E90: Practitioner: invalid password.
     */
    const val PRACTITIONER_INVALID_PASSWORD = 90

    /**
     * E91: Practitioner: passwords mismatch.
     */
    const val PRACTITIONER_MISMATCH_PASSWORD = 91

    /**
     * E92: Practitioner: Patient wrong data.
     */
    const val PRACTITIONER_WRONG_PATIENT_DATA = 92

    /**
     * E93: Practitioner: account is not verified.
     */
    const val PRACTITIONER_NOT_VERIFIED = 93

    /**
     * E94: Patient: patient does not exist.
     */
    const val PATIENT_DOES_NOT_EXIST = 94

    /**
     * E95: Profile: profile does not exist.
     */
    const val PROFILE_DOES_NOT_EXIST = 95

    /**
     * E100: Profile: Profile parameter missing.
     */
    const val PROFILE_PARAM_MISSING = 100

    /**
     * E101: Profile: Email parameter missing.
     */
    const val EMAIL_PARAM_MISSING = 101

    /**
     * E102: Token: Token parameter missing.
     */
    const val TOKEN_PARAM_MISSING = 102

    /**
     * E103: Patient verification token missing.
     */
    const val PATIENT_MISSING_VERIFICATION_TOKEN = 103

    /**
     * E104: Patient revoke token missing.
     */
    const val PATIENT_MISSING_REVOKE_TOKEN = 104

    /**
     * E105: Patient first name missing
     */
    const val PATIENT_MISSING_FIRSTNAME = 105

    /**
     * E106: Patient last name missing.
     */
    const val PATIENT_MISSING_LASTNAME = 106

    /**
     * E107: Patient gender missing.
     */
    const val PATIENT_MISSING_GENDER = 107

    /**
     * E108: Patient birth date missing.
     */
    const val PATIENT_MISSING_BIRTHDATE = 108

    /**
     * E109: Patient email address missing.
     */
    const val PATIENT_MISSING_EMAIL = 109

    /**
     * E110: Patient risk indicator missing.
     */
    const val PATIENT_MISSING_RISKINDICATOR = 110

    /**
     * E111: Patient account does not exist.
     */
    const val PATIENT_ACCOUNT_NOT_EXIST = 111

    /**
     * E112: Practitioner secret code is missing.
     */
    const val PRACTITIONER_MISSING_SECRET_CODE = 112

    /**
     * E113: Practitioner secret code is invalid.
     */
    const val PRACTITIONER_SECRET_CODE_INCORRECT = 113

    /**
     * E114: Practitioner RPPS number is missing.
     */
    const val PRACTITIONER_MISSING_RPPS_NUMBER = 114

    /**
     * E115: something went wrong.
     */
    const val SOMETHING_WENT_WRONG_ERROR = 115

    /**
     * E116: Wrong method.
     */
    const val WRONG_METHOD = 116

    /**
     * E245: Contact form subject missing.
     */
    const val CONTACT_FORM_SUBJECT_MISSING = 245

    /**
     * E246: Contact form name missing.
     */
    const val CONTACT_FORM_NAME_MISSING = 246

    /**
     * E247: Contact form message missing.
     */
    const val CONTACT_FORM_MESSAGE_MISSING = 247

    /**
     * E248: Contact form email missing.
     */
    const val CONTACT_FORM_EMAIL_MISSING = 248

    /**
     * E249: payment failed.
     */
    const val PAYMENT_FAILED = 249

    /**
     * E250: missing risk factors.
     */
    const val PATIENT_MISSING_RISKFACTORS = 250

    /**
     * E251: Object does not exists.
     */
    const val OBJECT_DOES_NOT_EXIST = 251

    /**
     * E252: Wrong body.
     */
    const val OBJECT_WRONG_BODY = 252

    /**
     * E253: Client is not privileged.
     */
    const val CLIENT_NOT_PRIVILEGED = 253

    /**
     * E254: Client does not exist.
     */
    const val CLIENT_DOES_NOT_EXIST = 254

    /**
     * E255: Code does not exist.
     */
    const val OAUTH_CODE_DOES_NOT_EXIST = 255

    /**
     * E256: Redirect_uri does not match.
     */
    const val REDIRECT_URI_DOES_NOT_MATCH = 256

    /**
     * E257: Client does not match.
     */
    const val CLIENT_DOES_NOT_MATCH = 257

    /**
     * E258: Missing duration.
     */
    const val MISSING_DURATION = 258

    /**
     * E259: Missing redirect_uri.
     */
    const val MISSING_REDIRECT_URI = 259

    /**
     * E260: Missing client_id.
     */
    const val MISSING_CLIENT_ID = 260

    /**
     * E261: Missing state.
     */
    const val MISSING_STATE = 261

    /**
     * E262: Server is in maintenance.
     */
    const val IN_MAINTENANCE = 262

    /**
     * E290: Office name is missing.
     */
    const val OFFICE_MISSING_NAME = 290

    /**
     * E309: Trying to delete own self.
     */
    const val COWORKER_DELETE_SELF = 309

    /**
     * E310: Coworker does not exist.
     */
    const val COWORKER_DOES_NOT_EXIST = 310

    /**
     * E311: Invalid refresh token.
     */
    const val PRACTITIONER_REFRESH_TOKEN_INVALID = 311

    /**
     * E312: Missing login data. Should be either (email, password) or (practitioner_id, refresh_token)
     */
    const val PRACTITIONER_MISSING_LOGIN_DATA = 312

    /**
     * E313: Missing serial or mac_address.
     */
    const val MISSING_SERIAL_OR_MAC_ADDRESS = 313

    /**
     * E314: Brushing does not exist.
     */
    const val BRUSHING_DOES_NOT_EXIST = 314

    /**
     * E315: Some fields are missing.
     */
    const val GENERIC_MISSING_FIELDS = 315

    /**
     * E316: Email already exists.
     */
    const val ACCOUNT_EMAIL_ALREADY_EXIST = 316

    /**
     * E316: Appid already exists.
     */
    const val ACCOUNT_APPID_ALREADY_EXIST = 316

    /**
     * E317: Practitioner with this email already exists.
     */
    const val PRACTITIONER_EMAIL_ALREADY_EXIST = 317

    /**
     * E318: Patient is not linked to the dentist.
     */
    const val PATIENT_NOT_LINKED = 318

    /**
     * E319: Patient has not accepted the request.
     */
    const val PATIENT_HAS_NOT_ACCEPTED_REQUEST = 319

    /**
     * E320: Office address is missing.
     */
    const val OFFICE_MISSING_ADDRESS = 320

    /**
     * E321: Office zipcode is missing.
     */
    const val OFFICE_MISSING_ZIPCODE = 321

    /**
     * E322: Office city is missing.
     */
    const val OFFICE_MISSING_CITY = 322

    /**
     * E323: Office country is missing.
     */
    const val OFFICE_MISSING_COUNTRY = 323

    /**
     * E324: Office state is missing.
     */
    const val OFFICE_MISSING_STATE = 324

    /**
     * E325: Office wrong data.
     */
    const val OFFICE_WRONG_DATA = 325

    /**
     * E326: Use temporary passwords.
     */
    const val TEMPORARY_PASSWORD_ACCOUNT = 326

    /**
     * E327: Invalid authorization code.
     */
    const val OAUTH_CODE_EXPIRED = 327

    /**
     * E328: Invalid authorization code.
     */
    const val OAUTH_CODE_NOT_VALIDATED = 328

    /**
     * E329: Message wrong title.
     */
    const val MESSAGE_WRONG_TITLE = 329

    /**
     * E330: Message wrong body.
     */
    const val MESSAGE_WRONG_BODY = 330

    /**
     * E331: FB account already exists.
     */
    const val FACEBOOK_ACCOUNT_ALREADY_EXIST = 331

    /**
     * E332: missing DATAPP instructions.
     */
    const val MISSING_DATAPP_INSTRUCTIONS = 332

    /**
     * E400: Shopify login email missing.
     */
    const val SHOPIFY_EMAIL_MISSING = 400

    /**
     * E401: Shopify login invalid account.
     */
    const val SHOPIFY_INVALID_ACCOUNT = 401

    /**
     * E402: Shopify account not set.
     */
    const val SHOPIFY_ACCOUNT_DOES_NOT_EXIST = 402

    /**
     * E403: Shopify bad password.
     */
    const val SHOPIFY_BAD_PASSWORD = 403

    /**
     * E404: Shopify password different verification.
     */
    const val SHOPIFY_BAD_VERIFICATION_PASSWORD = 404

    /**
     * E404: Brush Head information does not exists
     */
    const val BRUSH_HEAD_NON_EXISTING = 404

    /**
     * E405: Shopify missing parameters.
     */
    const val SHOPIFY_MISS_PARAM_RESET_PASSWORD = 405

    /**
     * E406: Account with appid.
     */
    const val SHOPIFY_ACCOUNT_WITH_APPID = 406

    /**
     * E407: Missing shopify store domain.
     */
    const val MISSING_SHOPIFY_STORE_DOMAIN = 407

    /**
     * E408: Missing shopify HMAC.
     */
    const val MISSING_SHOPIFY_HMAC = 408

    /**
     * E409: Shopify store does not exist.
     */
    const val SHOPIFY_STORE_DOES_NOT_EXIST = 409

    /**
     * E410: Something went wrong.
     */
    const val SHOPIFY_GENERAL_ERROR = 410

    /**
     * E411: file does not exist.
     */
    const val TEST_ERROR = 411

    /**
     * E412: Invalid reCAPTCHA.
     */
    const val SHOPIFY_RECPATCHA_ERROR = 412

    /**
     * E413: profile_picture not set or missing.
     */
    const val PICTURE_NOT_SET = 413

    /**
     * E414: Shopify store not valid.
     */
    const val WRONG_SHOPIFY_STORE = 414

    /**
     * E415: An error has occured
     */
    const val GET_MY_DATA_ERROR = 415

    /**
     * E416: parental consent required.
     */
    const val PARENTAL_CONSENT_REQUIRED = 416

    /**
     * E417: parental email required.
     */
    const val PARENTAL_EMAIL_REQUIRED = 417

    /**
     * E417: parental consent undefined.
     */
    const val PARENTAL_CONSENT_UNDEFINED = 417

    /**
     * E418: Account: wrong data. Appid is missing.
     */
    const val ACCOUNT_MISSING_APPID = 418

    /**
     * E419: invalid app version.
     */
    const val INVALID_APP_VERSION = 419

    /**
     * E420: Wrong 'header' format.
     */
    const val WRONG_HEADER_FORMAT = 420

    /**
     * E421: missing required header.
     */
    const val MISSING_REQUIRED_HEADER = 421

    /**
     * E422: asset bundle does not exist.
     */
    const val ASSET_BUNDLE_DOES_NOT_EXIST = 422

    /**
     * E423: wrong version.
     */
    const val WRONG_BRUSHING_LAST_VERSION = 423

    /**
     * E424: exceeded number of requests.
     */
    const val THROTTLING_ERROR = 424

    /**
     * E425: account already exists.
     */
    const val ACCOUNT_ALREADY_EXIST = 425

    /**
     * E426: database switch is not permitted.
     */
    const val DATABASE_NOT_PERMITTED = 426

    /**
     * E427: the phone number is empty or the length of phone number is wrong.
     */
    const val ZHONGAN_1008 = 427

    /**
     * E428: incorrect format of phone number.
     */
    const val ZHONGAN_5004 = 428

    /**
     * E429: request frequency is too high, please try after 60 seconds.
     */
    const val ZHONGAN_21001 = 429

    /**
     * E430: request exceeded maximum limit within 24 hours for the same phone number.
     */
    const val ZHONGAN_21002 = 430

    /**
     * E431: an error occurred during the process.
     */
    const val AWS_SMS_ERROR = 431

    /**
     * E432: invalid wechat code or id.
     */
    const val WECHAT_INVALID_CREDENTIALS = 432

    /**
     * E433: Phone number already exists.
     */
    const val ACCOUNT_PHONE_NUMBER_ALREADY_EXIST = 433

    /**
     * E434: WeChat account already exist.
     */
    const val WECHAT_ACCOUNT_ALREADY_EXIST = 434

    /**
     * E435: Invalid migration token.
     */
    const val INVALID_MIGRATION_TOKEN = 435

    /**
     * E436: Migration token has expired.
     */
    const val MIGRATION_TOKEN_HAS_EXPIRED = 436

    /**
     * E450: Invalid Google credentials.
     */
    const val GOOGLE_INVALID_CREDENTIALS = 450

    /**
     * E456: Today question of the day has already been answered.
     */
    const val QUESTION_ALREADY_ANSWERED = 456

    /**
     * E467: Unable to connect to Amazon DRS.
     */
    const val AMAZON_DRS_UNABLE_TO_CONNECT = 467

    /**
     * E468: Amazon DRS authentication failed.
     */
    const val AMAZON_DRS_AUTHENTICATION_FAILED = 468
}
