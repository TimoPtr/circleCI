package cn.colgate.colgateconnect.auth.sms

import android.annotation.SuppressLint

@SuppressLint("SdkPublicClassInNonKolibreePackage", "DeobfuscatedPublicSdkClass")
data class SmsAuthViewState(
    val isLoading: Boolean = false,
    val isConfirmationCodeVisible: Boolean = false,
    val phoneNumberError: String = "",
    val confirmationCodeError: String = ""
) {

    fun withIsLoading(loading: Boolean) = copy(isLoading = loading)

    fun withIsConfirmationCodeVisible(visible: Boolean) = copy(isConfirmationCodeVisible = visible)

    fun withPhoneNumberError(error: String) = copy(phoneNumberError = error)

    fun withConfirmationCodeError(error: String) = copy(confirmationCodeError = error)

    fun clearErrors() = copy(phoneNumberError = "", confirmationCodeError = "")
}
