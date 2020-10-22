package com.kolibree.sdkws

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.network.NetworkConstants

/**
 * Created by aurelien on 15/09/15.
 *
 *
 * Project settings
 */
@VisibleForApp
object Constants {
    const val AVATAR_SIZE_PX = 600

    // Endpoints declaration
    const val SERVICE_BASE_ACCOUNT_URL = "/v3/accounts"
    const val SERVICE_BASE_ACCOUNT_URL_V4 = "/v4/accounts"
    const val SERVICE_CHANGE_PASSWORD =
        "/v1/accounts/%d/change_password/" // %d = account ID
    const val SERVICE_READ_INSTRUCTIONS = "/v1/accounts/%d/instructions/"
    const val SERVICE_UPLOAD_VIDEO = "/v1/accounts/%d/video_signed_url_v2/"
    const val SERVICE_REFRESH_TOKEN_V3 =
        NetworkConstants.SERVICE_REFRESH_TOKEN_V3 + "?access_token=%s&refresh_token=%s"
    const val SERVICE_GET_CHECKUP_DATA =
        "/v1/accounts/%d/profiles/%d/checkup/" // %d = account id, %d = profile id
    const val SERVICE_GET_GO_PIRATE_DATA =
        "/v1/accounts/%d/profiles/%d/go_pirate/" // %d = account id, %d = profile id
    const val SERVICE_UPDATE_GO_PIRATE_DATA =
        "/v1/accounts/%d/profiles/%d/go_pirate/" // %d = account id, %d = profile id
    const val SERVICE_REQUEST_TOKEN_FACEBOOK = "/v3/accounts/connectWithFacebook/"
    const val SERVICE_RESET_PASSWORD = "/v1/shopify/reset_password/"
    const val SERVICE_GET_DEFAULT_AVATARS = "/v1/defaultProfilePictures/"

    // profile id
    const val SERVICE_REVOKE_PRACTITIONERS =
        "/v1/practitioner/patient/access/?token=%s" // %s practitioner token
    const val SERVICE_GET_PRACTITIONERS =
        "/v1/practitioner/patient/accounts/%d/profiles/%d/get_practitioner/" // %d = account id, %d =
    const val SERVICE_CHECK_FW_UPDATE = "/v1/firmware/"

    // %s model, %s fw version string, hardware version string, %s GRU version string
    const val SERVICE_CHECK_GRU_UPDATE = "/v1/grudata/?model=%s&fw=%s&hw=%s&gru=%s"
    const val SERVICE_UPDATE_TOOTHBRUSH =
        "/v1/toothbrushes/accounts/%d/" // %d = account id
    const val SERVICE_UPLOAD_RAW_DATA =
        "/v1/accounts/%d/raw_data_signed_url_v2/android/" // %d = account id
    const val SERVICE_VALIDATE_CODE = "/v1/accounts/validate_code/"
    const val SERVICE_MAGIC_LINK_REQUEST = "/v1/accounts/authorization_code/"
    const val SERVICE_CHECK_UNDERAGED =
        "/v3/accounts/check_birthday/?birthday=%s&country=%s"
    const val START_PRO_URL = "/kp_start?lang=%s&verification_token=%s"

    // Colgate Connect Pro URLs
    const val COLGATE_PRO_STAGING = "https://colgate.kolibreepro.com/web"
    const val COLGATE_PRO_PRODUCTION = "https://connectpro.colgate.com/web"

    // Kolibree Pro URLs
    const val KOLIBREE_PRO_STAGING = "https://staging.kolibree.com"
    const val KOLIBREE_PRO_PRODUCTION = "https://www.kolibree.com"

    const val FIELD_FACEBOOK_ID = "facebook_id"
    const val FIELD_FACEBOOK_AUTH_TOKEN = "facebook_auth_token"
    const val FIELD_EMAIL = "email"
    const val FIELD_APP_ID = "appid"
}
