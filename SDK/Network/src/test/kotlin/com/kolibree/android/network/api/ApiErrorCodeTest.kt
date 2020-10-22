/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.api

import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** [ApiErrorCode] tests */
class ApiErrorCodeTest : BaseUnitTest() {

    @Test
    fun `value of UNKNOWN_ERROR is -2`() {
        assertEquals(-2, ApiErrorCode.UNKNOWN_ERROR)
    }

    @Test
    fun `value of NETWORK_ERROR is -1`() {
        assertEquals(-1, ApiErrorCode.NETWORK_ERROR)
    }

    @Test
    fun `value of MISSING_CLIENT_ID_OR_SIG is 1`() {
        assertEquals(1, ApiErrorCode.MISSING_CLIENT_ID_OR_SIG)
    }

    @Test
    fun `value of CLIENT_DOES_NOT_EXIST_1 is 2`() {
        assertEquals(2, ApiErrorCode.CLIENT_DOES_NOT_EXIST_1)
    }

    @Test
    fun `value of CLIENT_WRONG_SIGNATURE is 3`() {
        assertEquals(3, ApiErrorCode.CLIENT_WRONG_SIGNATURE)
    }

    @Test
    fun `value of ACCOUNT_DOES_NOT_EXIST is 4`() {
        assertEquals(4, ApiErrorCode.ACCOUNT_DOES_NOT_EXIST)
    }

    @Test
    fun `value of INVALID_ACCESS_TOKEN is 5`() {
        assertEquals(5, ApiErrorCode.INVALID_ACCESS_TOKEN)
    }

    @Test
    fun `value of ACCESS_TOKEN_HAS_EXPIRED is 6`() {
        assertEquals(6, ApiErrorCode.ACCESS_TOKEN_HAS_EXPIRED)
    }

    @Test
    fun `value of PROFILE_WRONG_ASSOCIATION is 7`() {
        assertEquals(7, ApiErrorCode.PROFILE_WRONG_ASSOCIATION)
    }

    @Test
    fun `value of ACCOUNT_EMAIL_MISSING is 8`() {
        assertEquals(8, ApiErrorCode.ACCOUNT_EMAIL_MISSING)
    }

    @Test
    fun `value of ACCOUNT_TYPE_MISSING is 9`() {
        assertEquals(9, ApiErrorCode.ACCOUNT_TYPE_MISSING)
    }

    @Test
    fun `value of PASSWORD_TYPE_MISSING is 10`() {
        assertEquals(10, ApiErrorCode.PASSWORD_TYPE_MISSING)
    }

    @Test
    fun `value of ACCOUNT_INVALID_CREDENTIALS is 11`() {
        assertEquals(11, ApiErrorCode.ACCOUNT_INVALID_CREDENTIALS)
    }

    @Test
    fun `value of INVALID_FACEBOOK_TOKEN is 12`() {
        assertEquals(12, ApiErrorCode.INVALID_FACEBOOK_TOKEN)
    }

    @Test
    fun `value of MISSING_GOOGLE_ID is 13`() {
        assertEquals(13, ApiErrorCode.MISSING_GOOGLE_ID)
    }

    @Test
    fun `value of PROFILE_MISSING_FIRST_NAME is 14`() {
        assertEquals(14, ApiErrorCode.PROFILE_MISSING_FIRST_NAME)
    }

    @Test
    fun `value of PROFILE_MISSING_LAST_NAME is 15`() {
        assertEquals(15, ApiErrorCode.PROFILE_MISSING_LAST_NAME)
    }

    @Test
    fun `value of PROFILE_MISSING_GENDER is 16`() {
        assertEquals(16, ApiErrorCode.PROFILE_MISSING_GENDER)
    }

    @Test
    fun `value of PROFILE_MISSING_BIRTHDAY is 17`() {
        assertEquals(17, ApiErrorCode.PROFILE_MISSING_BIRTHDAY)
    }

    @Test
    fun `value of ACCOUNT_MISSING_COUNTRY is 18`() {
        assertEquals(18, ApiErrorCode.ACCOUNT_MISSING_COUNTRY)
    }

    @Test
    fun `value of ACCOUNT_WRONG_DATA is 19`() {
        assertEquals(19, ApiErrorCode.ACCOUNT_WRONG_DATA)
    }

    @Test
    fun `value of PROFILE_WRONG_DATA is 20`() {
        assertEquals(20, ApiErrorCode.PROFILE_WRONG_DATA)
    }

    @Test
    fun `value of UNABLE_TO_CREATE_USER_IN_USER_API is 21`() {
        assertEquals(21, ApiErrorCode.UNABLE_TO_CREATE_USER_IN_USER_API)
    }

    @Test
    fun `value of ACCOUNT_CREATION_ERROR is 22`() {
        assertEquals(22, ApiErrorCode.ACCOUNT_CREATION_ERROR)
    }

    @Test
    fun `value of ACCOUNT_UPDATE_ERROR is 23`() {
        assertEquals(23, ApiErrorCode.ACCOUNT_UPDATE_ERROR)
    }

    @Test
    fun `value of INVALID_REFRESH_TOKEN is 24`() {
        assertEquals(24, ApiErrorCode.INVALID_REFRESH_TOKEN)
    }

    @Test
    fun `value of MISSING_OR_WRONG_PASSWORD is 25`() {
        assertEquals(25, ApiErrorCode.MISSING_OR_WRONG_PASSWORD)
    }

    @Test
    fun `value of PROFILE_DOES_NOT_EXIST_IN_USER_API is 26`() {
        assertEquals(26, ApiErrorCode.PROFILE_DOES_NOT_EXIST_IN_USER_API)
    }

    @Test
    fun `value of UNABLE_TO_UPDATE_PROFILE_USER_API is 27`() {
        assertEquals(27, ApiErrorCode.UNABLE_TO_UPDATE_PROFILE_USER_API)
    }

    @Test
    fun `value of UNABLE_TO_DELETE_PROFILE is 28`() {
        assertEquals(28, ApiErrorCode.UNABLE_TO_DELETE_PROFILE)
    }

    @Test
    fun `value of RESET_PASSWORD_TOKEN_EXPIRED is 33`() {
        assertEquals(33, ApiErrorCode.RESET_PASSWORD_TOKEN_EXPIRED)
    }

    @Test
    fun `value of WRONG_BRUSHING_PROFILE_ASSOCATION is 34`() {
        assertEquals(34, ApiErrorCode.WRONG_BRUSHING_PROFILE_ASSOCATION)
    }

    @Test
    fun `value of BRUSHING_MISSING_DURATION is 35`() {
        assertEquals(35, ApiErrorCode.BRUSHING_MISSING_DURATION)
    }

    @Test
    fun `value of BRUSHING_MISSING_GOAL_DURATION is 36`() {
        assertEquals(36, ApiErrorCode.BRUSHING_MISSING_GOAL_DURATION)
    }

    @Test
    fun `value of BRUSHING_DATETIME_MISSING is 38`() {
        assertEquals(38, ApiErrorCode.BRUSHING_DATETIME_MISSING)
    }

    @Test
    fun `value of ACCOUNT_ADDITIONAL_INFO_NEEDED is 39`() {
        assertEquals(39, ApiErrorCode.ACCOUNT_ADDITIONAL_INFO_NEEDED)
    }

    @Test
    fun `value of INVALID_PERMISSION is 40`() {
        assertEquals(40, ApiErrorCode.INVALID_PERMISSION)
    }

    @Test
    fun `value of PICTURE_GET_URL_MISSING is 41`() {
        assertEquals(41, ApiErrorCode.PICTURE_GET_URL_MISSING)
    }

    @Test
    fun `value of PICTURE_GET_URL_WRONG_VALUE is 42`() {
        assertEquals(42, ApiErrorCode.PICTURE_GET_URL_WRONG_VALUE)
    }

    @Test
    fun `value of CANT_DELETE_OWNER_PROFILE is 43`() {
        assertEquals(43, ApiErrorCode.CANT_DELETE_OWNER_PROFILE)
    }

    @Test
    fun `value of CLIENT_WRONG_API_1 is 44`() {
        assertEquals(44, ApiErrorCode.CLIENT_WRONG_API_1)
    }

    @Test
    fun `value of CLIENT_WRONG_API_2 is 45`() {
        assertEquals(45, ApiErrorCode.CLIENT_WRONG_API_2)
    }

    @Test
    fun `value of UNSUFFICIENT_RIGHTS is 46`() {
        assertEquals(46, ApiErrorCode.UNSUFFICIENT_RIGHTS)
    }

    @Test
    fun `value of STAT_DOES_NOT_EXIST is 47`() {
        assertEquals(47, ApiErrorCode.STAT_DOES_NOT_EXIST)
    }

    @Test
    fun `value of BADGE_DOES_NOT_EXIST is 48`() {
        assertEquals(48, ApiErrorCode.BADGE_DOES_NOT_EXIST)
    }

    @Test
    fun `value of ACCOUNT_LEVEL_PERMISSION_FALSE is 49`() {
        assertEquals(49, ApiErrorCode.ACCOUNT_LEVEL_PERMISSION_FALSE)
    }

    @Test
    fun `value of MISSING_DENTEGRA_ACCOUNT_ID is 50`() {
        assertEquals(50, ApiErrorCode.MISSING_DENTEGRA_ACCOUNT_ID)
    }

    @Test
    fun `value of BRUSHING_FILE_DOES_NOT_EXIST is 51`() {
        assertEquals(51, ApiErrorCode.BRUSHING_FILE_DOES_NOT_EXIST)
    }

    @Test
    fun `value of NO_MODIFICATION_HISTORY is 52`() {
        assertEquals(52, ApiErrorCode.NO_MODIFICATION_HISTORY)
    }

    @Test
    fun `value of UNSUPPORTED_COUNTRY is 53`() {
        assertEquals(53, ApiErrorCode.UNSUPPORTED_COUNTRY)
    }

    @Test
    fun `value of MISSING_ORDER_ID is 54`() {
        assertEquals(54, ApiErrorCode.MISSING_ORDER_ID)
    }

    @Test
    fun `value of ORDER_DOES_NOT_EXIST is 55`() {
        assertEquals(55, ApiErrorCode.ORDER_DOES_NOT_EXIST)
    }

    @Test
    fun `value of WRONG_ORDER_STATUS is 56`() {
        assertEquals(56, ApiErrorCode.WRONG_ORDER_STATUS)
    }

    @Test
    fun `value of DENTEGRA_ACCOUNT_DOES_NOT_EXIST is 57`() {
        assertEquals(57, ApiErrorCode.DENTEGRA_ACCOUNT_DOES_NOT_EXIST)
    }

    @Test
    fun `value of DENTEGRA_PROFILE_DOES_NOT_EXIST is 58`() {
        assertEquals(58, ApiErrorCode.DENTEGRA_PROFILE_DOES_NOT_EXIST)
    }

    @Test
    fun `value of MISSING_TOKEN_Q_ARG is 59`() {
        assertEquals(59, ApiErrorCode.MISSING_TOKEN_Q_ARG)
    }

    @Test
    fun `value of WRONG_TOKEN_FORMAT is 60`() {
        assertEquals(60, ApiErrorCode.WRONG_TOKEN_FORMAT)
    }

    @Test
    fun `value of NO_USER_TOKEN is 61`() {
        assertEquals(61, ApiErrorCode.NO_USER_TOKEN)
    }

    @Test
    fun `value of TOKEN_HAS_EXPIRED is 62`() {
        assertEquals(62, ApiErrorCode.TOKEN_HAS_EXPIRED)
    }

    @Test
    fun `value of SHIPPING_METHOD_DOES_NOT_EXIST is 63`() {
        assertEquals(63, ApiErrorCode.SHIPPING_METHOD_DOES_NOT_EXIST)
    }

    @Test
    fun `value of INACTIVE_SHIPPING_METHOD is 64`() {
        assertEquals(64, ApiErrorCode.INACTIVE_SHIPPING_METHOD)
    }

    @Test
    fun `value of ACCOUNT_WITHOUT_PASSWORD is 65`() {
        assertEquals(65, ApiErrorCode.ACCOUNT_WITHOUT_PASSWORD)
    }

    @Test
    fun `value of DUPLICATE_DENTEGRA_ACCOUNT_ID is 66`() {
        assertEquals(66, ApiErrorCode.DUPLICATE_DENTEGRA_ACCOUNT_ID)
    }

    @Test
    fun `value of DUPLICATE_DENTEGRA_PROFILE_ID_1 is 67`() {
        assertEquals(67, ApiErrorCode.DUPLICATE_DENTEGRA_PROFILE_ID_1)
    }

    @Test
    fun `value of DUPLICATE_DENTEGRA_PROFILE_ID_2 is 68`() {
        assertEquals(68, ApiErrorCode.DUPLICATE_DENTEGRA_PROFILE_ID_2)
    }

    @Test
    fun `value of DUPLICATE_DENTEGRA_PROFILE_ID_3 is 69`() {
        assertEquals(69, ApiErrorCode.DUPLICATE_DENTEGRA_PROFILE_ID_3)
    }

    @Test
    fun `value of WRONG_ARGUMENTS is 70`() {
        assertEquals(70, ApiErrorCode.WRONG_ARGUMENTS)
    }

    @Test
    fun `value of DUPLICATE_DENTEGRA_ACCOUNT_ID_2 is 71`() {
        assertEquals(71, ApiErrorCode.DUPLICATE_DENTEGRA_ACCOUNT_ID_2)
    }

    @Test
    fun `value of INVALID_CUSTOM_PAYMENT is 72`() {
        assertEquals(72, ApiErrorCode.INVALID_CUSTOM_PAYMENT)
    }

    @Test
    fun `value of PROFILE_DOES_NOT_EXIST_BIS is 73`() {
        assertEquals(73, ApiErrorCode.PROFILE_DOES_NOT_EXIST_BIS)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_ACCESS_TOKEN is 75`() {
        assertEquals(75, ApiErrorCode.PRACTITIONER_MISSING_ACCESS_TOKEN)
    }

    @Test
    fun `value of PRACTITIONER_INVALID_ACCESS_TOKEN is 76`() {
        assertEquals(76, ApiErrorCode.PRACTITIONER_INVALID_ACCESS_TOKEN)
    }

    @Test
    fun `value of PRACTITIONER_EXPIRED_ACCESS_TOKEN is 77`() {
        assertEquals(77, ApiErrorCode.PRACTITIONER_EXPIRED_ACCESS_TOKEN)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_EMAIL is 78`() {
        assertEquals(78, ApiErrorCode.PRACTITIONER_MISSING_EMAIL)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_PASSWORD is 79`() {
        assertEquals(79, ApiErrorCode.PRACTITIONER_MISSING_PASSWORD)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_FIRST_NAME is 80`() {
        assertEquals(80, ApiErrorCode.PRACTITIONER_MISSING_FIRST_NAME)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_LAST_NAME is 81`() {
        assertEquals(81, ApiErrorCode.PRACTITIONER_MISSING_LAST_NAME)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_GENDER is 82`() {
        assertEquals(82, ApiErrorCode.PRACTITIONER_MISSING_GENDER)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_BIRTH_DATE is 83`() {
        assertEquals(83, ApiErrorCode.PRACTITIONER_MISSING_BIRTH_DATE)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_JOB is 84`() {
        assertEquals(84, ApiErrorCode.PRACTITIONER_MISSING_JOB)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_SPECIALITY is 85`() {
        assertEquals(85, ApiErrorCode.PRACTITIONER_MISSING_SPECIALITY)
    }

    @Test
    fun `value of PRACTITIONER_PASSWORD_COMPLEXITY_NOT_MET is 86`() {
        assertEquals(86, ApiErrorCode.PRACTITIONER_PASSWORD_COMPLEXITY_NOT_MET)
    }

    @Test
    fun `value of PRACTITIONER_WRONG_DATA is 87`() {
        assertEquals(87, ApiErrorCode.PRACTITIONER_WRONG_DATA)
    }

    @Test
    fun `value of PRACTITIONER_DOESNT_EXIST is 88`() {
        assertEquals(88, ApiErrorCode.PRACTITIONER_DOESNT_EXIST)
    }

    @Test
    fun `value of PRACTITIONER_INVALID_EMAIL_OR_PASSWORD is 89`() {
        assertEquals(89, ApiErrorCode.PRACTITIONER_INVALID_EMAIL_OR_PASSWORD)
    }

    @Test
    fun `value of PRACTITIONER_INVALID_PASSWORD is 90`() {
        assertEquals(90, ApiErrorCode.PRACTITIONER_INVALID_PASSWORD)
    }

    @Test
    fun `value of PRACTITIONER_MISMATCH_PASSWORD is 91`() {
        assertEquals(91, ApiErrorCode.PRACTITIONER_MISMATCH_PASSWORD)
    }

    @Test
    fun `value of PRACTITIONER_WRONG_PATIENT_DATA is 92`() {
        assertEquals(92, ApiErrorCode.PRACTITIONER_WRONG_PATIENT_DATA)
    }

    @Test
    fun `value of PRACTITIONER_NOT_VERIFIED is 93`() {
        assertEquals(93, ApiErrorCode.PRACTITIONER_NOT_VERIFIED)
    }

    @Test
    fun `value of PATIENT_DOES_NOT_EXIST is 94`() {
        assertEquals(94, ApiErrorCode.PATIENT_DOES_NOT_EXIST)
    }

    @Test
    fun `value of PROFILE_DOES_NOT_EXIST is 95`() {
        assertEquals(95, ApiErrorCode.PROFILE_DOES_NOT_EXIST)
    }

    @Test
    fun `value of PROFILE_PARAM_MISSING is 100`() {
        assertEquals(100, ApiErrorCode.PROFILE_PARAM_MISSING)
    }

    @Test
    fun `value of EMAIL_PARAM_MISSING is 101`() {
        assertEquals(101, ApiErrorCode.EMAIL_PARAM_MISSING)
    }

    @Test
    fun `value of TOKEN_PARAM_MISSING is 102`() {
        assertEquals(102, ApiErrorCode.TOKEN_PARAM_MISSING)
    }

    @Test
    fun `value of PATIENT_MISSING_VERIFICATION_TOKEN is 103`() {
        assertEquals(103, ApiErrorCode.PATIENT_MISSING_VERIFICATION_TOKEN)
    }

    @Test
    fun `value of PATIENT_MISSING_REVOKE_TOKEN is 104`() {
        assertEquals(104, ApiErrorCode.PATIENT_MISSING_REVOKE_TOKEN)
    }

    @Test
    fun `value of PATIENT_MISSING_FIRSTNAME is 105`() {
        assertEquals(105, ApiErrorCode.PATIENT_MISSING_FIRSTNAME)
    }

    @Test
    fun `value of PATIENT_MISSING_LASTNAME is 106`() {
        assertEquals(106, ApiErrorCode.PATIENT_MISSING_LASTNAME)
    }

    @Test
    fun `value of PATIENT_MISSING_GENDER is 107`() {
        assertEquals(107, ApiErrorCode.PATIENT_MISSING_GENDER)
    }

    @Test
    fun `value of PATIENT_MISSING_BIRTHDATE is 108`() {
        assertEquals(108, ApiErrorCode.PATIENT_MISSING_BIRTHDATE)
    }

    @Test
    fun `value of PATIENT_MISSING_EMAIL is 109`() {
        assertEquals(109, ApiErrorCode.PATIENT_MISSING_EMAIL)
    }

    @Test
    fun `value of PATIENT_MISSING_RISKINDICATOR is 110`() {
        assertEquals(110, ApiErrorCode.PATIENT_MISSING_RISKINDICATOR)
    }

    @Test
    fun `value of PATIENT_ACCOUNT_NOT_EXIST is 111`() {
        assertEquals(111, ApiErrorCode.PATIENT_ACCOUNT_NOT_EXIST)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_SECRET_CODE is 112`() {
        assertEquals(112, ApiErrorCode.PRACTITIONER_MISSING_SECRET_CODE)
    }

    @Test
    fun `value of PRACTITIONER_SECRET_CODE_INCORRECT is 113`() {
        assertEquals(113, ApiErrorCode.PRACTITIONER_SECRET_CODE_INCORRECT)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_RPPS_NUMBER is 114`() {
        assertEquals(114, ApiErrorCode.PRACTITIONER_MISSING_RPPS_NUMBER)
    }

    @Test
    fun `value of SOMETHING_WENT_WRONG_ERROR is 115`() {
        assertEquals(115, ApiErrorCode.SOMETHING_WENT_WRONG_ERROR)
    }

    @Test
    fun `value of WRONG_METHOD is 116`() {
        assertEquals(116, ApiErrorCode.WRONG_METHOD)
    }

    @Test
    fun `value of CONTACT_FORM_SUBJECT_MISSING is 245`() {
        assertEquals(245, ApiErrorCode.CONTACT_FORM_SUBJECT_MISSING)
    }

    @Test
    fun `value of CONTACT_FORM_NAME_MISSING is 246`() {
        assertEquals(246, ApiErrorCode.CONTACT_FORM_NAME_MISSING)
    }

    @Test
    fun `value of CONTACT_FORM_MESSAGE_MISSING is 247`() {
        assertEquals(247, ApiErrorCode.CONTACT_FORM_MESSAGE_MISSING)
    }

    @Test
    fun `value of CONTACT_FORM_EMAIL_MISSING is 248`() {
        assertEquals(248, ApiErrorCode.CONTACT_FORM_EMAIL_MISSING)
    }

    @Test
    fun `value of PAYMENT_FAILED is 249`() {
        assertEquals(249, ApiErrorCode.PAYMENT_FAILED)
    }

    @Test
    fun `value of PATIENT_MISSING_RISKFACTORS is 250`() {
        assertEquals(250, ApiErrorCode.PATIENT_MISSING_RISKFACTORS)
    }

    @Test
    fun `value of OBJECT_DOES_NOT_EXIST is 251`() {
        assertEquals(251, ApiErrorCode.OBJECT_DOES_NOT_EXIST)
    }

    @Test
    fun `value of OBJECT_WRONG_BODY is 252`() {
        assertEquals(252, ApiErrorCode.OBJECT_WRONG_BODY)
    }

    @Test
    fun `value of CLIENT_NOT_PRIVILEGED is 253`() {
        assertEquals(253, ApiErrorCode.CLIENT_NOT_PRIVILEGED)
    }

    @Test
    fun `value of CLIENT_DOES_NOT_EXIST is 254`() {
        assertEquals(254, ApiErrorCode.CLIENT_DOES_NOT_EXIST)
    }

    @Test
    fun `value of OAUTH_CODE_DOES_NOT_EXIST is 255`() {
        assertEquals(255, ApiErrorCode.OAUTH_CODE_DOES_NOT_EXIST)
    }

    @Test
    fun `value of REDIRECT_URI_DOES_NOT_MATCH is 256`() {
        assertEquals(256, ApiErrorCode.REDIRECT_URI_DOES_NOT_MATCH)
    }

    @Test
    fun `value of CLIENT_DOES_NOT_MATCH is 257`() {
        assertEquals(257, ApiErrorCode.CLIENT_DOES_NOT_MATCH)
    }

    @Test
    fun `value of MISSING_DURATION is 258`() {
        assertEquals(258, ApiErrorCode.MISSING_DURATION)
    }

    @Test
    fun `value of MISSING_REDIRECT_URI is 259`() {
        assertEquals(259, ApiErrorCode.MISSING_REDIRECT_URI)
    }

    @Test
    fun `value of MISSING_CLIENT_ID is 260`() {
        assertEquals(260, ApiErrorCode.MISSING_CLIENT_ID)
    }

    @Test
    fun `value of MISSING_STATE is 261`() {
        assertEquals(261, ApiErrorCode.MISSING_STATE)
    }

    @Test
    fun `value of IN_MAINTENANCE is 262`() {
        assertEquals(262, ApiErrorCode.IN_MAINTENANCE)
    }

    @Test
    fun `value of OFFICE_MISSING_NAME is 290`() {
        assertEquals(290, ApiErrorCode.OFFICE_MISSING_NAME)
    }

    @Test
    fun `value of COWORKER_DELETE_SELF is 309`() {
        assertEquals(309, ApiErrorCode.COWORKER_DELETE_SELF)
    }

    @Test
    fun `value of COWORKER_DOES_NOT_EXIST is 310`() {
        assertEquals(310, ApiErrorCode.COWORKER_DOES_NOT_EXIST)
    }

    @Test
    fun `value of PRACTITIONER_REFRESH_TOKEN_INVALID is 311`() {
        assertEquals(311, ApiErrorCode.PRACTITIONER_REFRESH_TOKEN_INVALID)
    }

    @Test
    fun `value of PRACTITIONER_MISSING_LOGIN_DATA is 312`() {
        assertEquals(312, ApiErrorCode.PRACTITIONER_MISSING_LOGIN_DATA)
    }

    @Test
    fun `value of MISSING_SERIAL_OR_MAC_ADDRESS is 313`() {
        assertEquals(313, ApiErrorCode.MISSING_SERIAL_OR_MAC_ADDRESS)
    }

    @Test
    fun `value of BRUSHING_DOES_NOT_EXIST is 314`() {
        assertEquals(314, ApiErrorCode.BRUSHING_DOES_NOT_EXIST)
    }

    @Test
    fun `value of GENERIC_MISSING_FIELDS is 315`() {
        assertEquals(315, ApiErrorCode.GENERIC_MISSING_FIELDS)
    }

    @Test
    fun `value of ACCOUNT_EMAIL_ALREADY_EXIST is 316`() {
        assertEquals(316, ApiErrorCode.ACCOUNT_EMAIL_ALREADY_EXIST)
    }

    @Test
    fun `value of ACCOUNT_APPID_ALREADY_EXIST is 316`() {
        assertEquals(316, ApiErrorCode.ACCOUNT_APPID_ALREADY_EXIST)
    }

    @Test
    fun `value of PRACTITIONER_EMAIL_ALREADY_EXIST is 317`() {
        assertEquals(317, ApiErrorCode.PRACTITIONER_EMAIL_ALREADY_EXIST)
    }

    @Test
    fun `value of PATIENT_NOT_LINKED is 318`() {
        assertEquals(318, ApiErrorCode.PATIENT_NOT_LINKED)
    }

    @Test
    fun `value of PATIENT_HAS_NOT_ACCEPTED_REQUEST is 319`() {
        assertEquals(319, ApiErrorCode.PATIENT_HAS_NOT_ACCEPTED_REQUEST)
    }

    @Test
    fun `value of OFFICE_MISSING_ADDRESS is 320`() {
        assertEquals(320, ApiErrorCode.OFFICE_MISSING_ADDRESS)
    }

    @Test
    fun `value of OFFICE_MISSING_ZIPCODE is 321`() {
        assertEquals(321, ApiErrorCode.OFFICE_MISSING_ZIPCODE)
    }

    @Test
    fun `value of OFFICE_MISSING_CITY is 322`() {
        assertEquals(322, ApiErrorCode.OFFICE_MISSING_CITY)
    }

    @Test
    fun `value of OFFICE_MISSING_COUNTRY is 323`() {
        assertEquals(323, ApiErrorCode.OFFICE_MISSING_COUNTRY)
    }

    @Test
    fun `value of OFFICE_MISSING_STATE is 324`() {
        assertEquals(324, ApiErrorCode.OFFICE_MISSING_STATE)
    }

    @Test
    fun `value of OFFICE_WRONG_DATA is 325`() {
        assertEquals(325, ApiErrorCode.OFFICE_WRONG_DATA)
    }

    @Test
    fun `value of TEMPORARY_PASSWORD_ACCOUNT is 326`() {
        assertEquals(326, ApiErrorCode.TEMPORARY_PASSWORD_ACCOUNT)
    }

    @Test
    fun `value of OAUTH_CODE_EXPIRED is 327`() {
        assertEquals(327, ApiErrorCode.OAUTH_CODE_EXPIRED)
    }

    @Test
    fun `value of OAUTH_CODE_NOT_VALIDATED is 328`() {
        assertEquals(328, ApiErrorCode.OAUTH_CODE_NOT_VALIDATED)
    }

    @Test
    fun `value of MESSAGE_WRONG_TITLE is 329`() {
        assertEquals(329, ApiErrorCode.MESSAGE_WRONG_TITLE)
    }

    @Test
    fun `value of MESSAGE_WRONG_BODY is 330`() {
        assertEquals(330, ApiErrorCode.MESSAGE_WRONG_BODY)
    }

    @Test
    fun `value of FACEBOOK_ACCOUNT_ALREADY_EXIST is 331`() {
        assertEquals(331, ApiErrorCode.FACEBOOK_ACCOUNT_ALREADY_EXIST)
    }

    @Test
    fun `value of MISSING_DATAPP_INSTRUCTIONS is 332`() {
        assertEquals(332, ApiErrorCode.MISSING_DATAPP_INSTRUCTIONS)
    }

    @Test
    fun `value of SHOPIFY_EMAIL_MISSING is 400`() {
        assertEquals(400, ApiErrorCode.SHOPIFY_EMAIL_MISSING)
    }

    @Test
    fun `value of SHOPIFY_INVALID_ACCOUNT is 401`() {
        assertEquals(401, ApiErrorCode.SHOPIFY_INVALID_ACCOUNT)
    }

    @Test
    fun `value of SHOPIFY_ACCOUNT_DOES_NOT_EXIST is 402`() {
        assertEquals(402, ApiErrorCode.SHOPIFY_ACCOUNT_DOES_NOT_EXIST)
    }

    @Test
    fun `value of SHOPIFY_BAD_PASSWORD is 403`() {
        assertEquals(403, ApiErrorCode.SHOPIFY_BAD_PASSWORD)
    }

    @Test
    fun `value of SHOPIFY_BAD_VERIFICATION_PASSWORD is 404`() {
        assertEquals(404, ApiErrorCode.SHOPIFY_BAD_VERIFICATION_PASSWORD)
    }

    @Test
    fun `value of BRUSH_HEAD_NON_EXISTING is 404`() {
        assertEquals(404, ApiErrorCode.BRUSH_HEAD_NON_EXISTING)
    }

    @Test
    fun `value of SHOPIFY_MISS_PARAM_RESET_PASSWORD is 405`() {
        assertEquals(405, ApiErrorCode.SHOPIFY_MISS_PARAM_RESET_PASSWORD)
    }

    @Test
    fun `value of SHOPIFY_ACCOUNT_WITH_APPID is 406`() {
        assertEquals(406, ApiErrorCode.SHOPIFY_ACCOUNT_WITH_APPID)
    }

    @Test
    fun `value of MISSING_SHOPIFY_STORE_DOMAIN is 407`() {
        assertEquals(407, ApiErrorCode.MISSING_SHOPIFY_STORE_DOMAIN)
    }

    @Test
    fun `value of MISSING_SHOPIFY_HMAC is 408`() {
        assertEquals(408, ApiErrorCode.MISSING_SHOPIFY_HMAC)
    }

    @Test
    fun `value of SHOPIFY_STORE_DOES_NOT_EXIST is 409`() {
        assertEquals(409, ApiErrorCode.SHOPIFY_STORE_DOES_NOT_EXIST)
    }

    @Test
    fun `value of SHOPIFY_GENERAL_ERROR is 410`() {
        assertEquals(410, ApiErrorCode.SHOPIFY_GENERAL_ERROR)
    }

    @Test
    fun `value of TEST_ERROR is 411`() {
        assertEquals(411, ApiErrorCode.TEST_ERROR)
    }

    @Test
    fun `value of SHOPIFY_RECPATCHA_ERROR is 412`() {
        assertEquals(412, ApiErrorCode.SHOPIFY_RECPATCHA_ERROR)
    }

    @Test
    fun `value of PICTURE_NOT_SET is 413`() {
        assertEquals(413, ApiErrorCode.PICTURE_NOT_SET)
    }

    @Test
    fun `value of WRONG_SHOPIFY_STORE is 414`() {
        assertEquals(414, ApiErrorCode.WRONG_SHOPIFY_STORE)
    }

    @Test
    fun `value of GET_MY_DATA_ERROR is 415`() {
        assertEquals(415, ApiErrorCode.GET_MY_DATA_ERROR)
    }

    @Test
    fun `value of PARENTAL_CONSENT_REQUIRED is 416`() {
        assertEquals(416, ApiErrorCode.PARENTAL_CONSENT_REQUIRED)
    }

    @Test
    fun `value of PARENTAL_EMAIL_REQUIRED is 417`() {
        assertEquals(417, ApiErrorCode.PARENTAL_EMAIL_REQUIRED)
    }

    @Test
    fun `value of PARENTAL_CONSENT_UNDEFINED is 417`() {
        assertEquals(417, ApiErrorCode.PARENTAL_CONSENT_UNDEFINED)
    }

    @Test
    fun `value of ACCOUNT_MISSING_APPID is 418`() {
        assertEquals(418, ApiErrorCode.ACCOUNT_MISSING_APPID)
    }

    @Test
    fun `value of INVALID_APP_VERSION is 419`() {
        assertEquals(419, ApiErrorCode.INVALID_APP_VERSION)
    }

    @Test
    fun `value of WRONG_HEADER_FORMAT is 420`() {
        assertEquals(420, ApiErrorCode.WRONG_HEADER_FORMAT)
    }

    @Test
    fun `value of MISSING_REQUIRED_HEADER is 421`() {
        assertEquals(421, ApiErrorCode.MISSING_REQUIRED_HEADER)
    }

    @Test
    fun `value of ASSET_BUNDLE_DOES_NOT_EXIST is 422`() {
        assertEquals(422, ApiErrorCode.ASSET_BUNDLE_DOES_NOT_EXIST)
    }

    @Test
    fun `value of WRONG_BRUSHING_LAST_VERSION is 423`() {
        assertEquals(423, ApiErrorCode.WRONG_BRUSHING_LAST_VERSION)
    }

    @Test
    fun `value of THROTTLING_ERROR is 424`() {
        assertEquals(424, ApiErrorCode.THROTTLING_ERROR)
    }

    @Test
    fun `value of ACCOUNT_ALREADY_EXIST is 425`() {
        assertEquals(425, ApiErrorCode.ACCOUNT_ALREADY_EXIST)
    }

    @Test
    fun `value of DATABASE_NOT_PERMITTED is 426`() {
        assertEquals(426, ApiErrorCode.DATABASE_NOT_PERMITTED)
    }

    @Test
    fun `value of ZHONGAN_1008 is 427`() {
        assertEquals(427, ApiErrorCode.ZHONGAN_1008)
    }

    @Test
    fun `value of ZHONGAN_5004 is 428`() {
        assertEquals(428, ApiErrorCode.ZHONGAN_5004)
    }

    @Test
    fun `value of ZHONGAN_21001 is 429`() {
        assertEquals(429, ApiErrorCode.ZHONGAN_21001)
    }

    @Test
    fun `value of ZHONGAN_21002 is 430`() {
        assertEquals(430, ApiErrorCode.ZHONGAN_21002)
    }

    @Test
    fun `value of AWS_SMS_ERROR is 431`() {
        assertEquals(431, ApiErrorCode.AWS_SMS_ERROR)
    }

    @Test
    fun `value of WECHAT_INVALID_CREDENTIALS is 432`() {
        assertEquals(432, ApiErrorCode.WECHAT_INVALID_CREDENTIALS)
    }

    @Test
    fun `value of ACCOUNT_PHONE_NUMBER_ALREADY_EXIST is 433`() {
        assertEquals(433, ApiErrorCode.ACCOUNT_PHONE_NUMBER_ALREADY_EXIST)
    }

    @Test
    fun `value of WECHAT_ACCOUNT_ALREADY_EXIST is 434`() {
        assertEquals(434, ApiErrorCode.WECHAT_ACCOUNT_ALREADY_EXIST)
    }

    @Test
    fun `value of INVALID_MIGRATION_TOKEN is 435`() {
        assertEquals(435, ApiErrorCode.INVALID_MIGRATION_TOKEN)
    }

    @Test
    fun `value of MIGRATION_TOKEN_HAS_EXPIRED is 436`() {
        assertEquals(436, ApiErrorCode.MIGRATION_TOKEN_HAS_EXPIRED)
    }

    @Test
    fun `value of GOOGLE_INVALID_CREDENTIALS is 450`() {
        assertEquals(450, ApiErrorCode.GOOGLE_INVALID_CREDENTIALS)
    }

    @Test
    fun `value of QUESTION_ALREADY_ANSWERED is 456`() {
        assertEquals(456, ApiErrorCode.QUESTION_ALREADY_ANSWERED)
    }
}
