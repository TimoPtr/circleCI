package com.kolibree.sdkws.data.request

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.google.gson.annotations.SerializedName
import com.kolibree.android.commons.DATE_FORMATTER
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

@Keep
data class CreateAccountData @VisibleForTesting internal constructor(
    @SerializedName("birthday")
    private val birthday: String?,

    @SerializedName("gender")
    @VisibleForTesting
    val gender: String,

    @SerializedName("survey_handedness")
    private val surveyHandedness: String?,

    @SerializedName("country")
    @VisibleForTesting
    val country: String?,

    @SerializedName("appid")
    @VisibleForTesting
    val appid: String?,

    @SerializedName("first_name")
    @VisibleForTesting
    val firstName: String?,

    @SerializedName("email")
    @VisibleForTesting
    val email: String?,

    @SerializedName("parental_email")
    @VisibleForTesting
    val parentalEmail: String?,

    @SerializedName("phone_number")
    @VisibleForTesting
    val phoneNumber: String?,

    @SerializedName("verification_token")
    @VisibleForTesting
    val verificationToken: String?,

    @SerializedName("verification_code")
    @VisibleForTesting
    val verificationCode: String?,

    @SerializedName("google_id")
    @VisibleForTesting
    val googleId: String?,

    @SerializedName("google_id_token")
    @VisibleForTesting
    val googleIdToken: String?,

    @Transient
    @VisibleForTesting
    val googleAvatarUrl: String?,

    @SerializedName("beta")
    @VisibleForTesting
    val isBetaAccount: Boolean?,

    @SerializedName("parental_consent")
    @VisibleForTesting
    val parentalConsentGiven: Boolean?,

    @SerializedName("commercial_opt_in")
    @VisibleForTesting
    val commercialSubscription: Boolean?,

    @SerializedName("picture")
    @VisibleForTesting
    val avatarUrl: String?
) {
    fun getBirthday(): LocalDate {
        return LocalDate.parse(birthday, DATE_FORMATTER)
    }

    fun getSurveyHandedness(): Handedness {
        return Handedness.findBySerializedName(surveyHandedness)
    }

    @Keep
    @Parcelize
    data class Builder(
        private var birthDate: String? = null,
        private var gender: String = Gender.UNKNOWN.serializedName,
        private var handedness: String? = null,
        private var country: String? = null,
        private var appid: String? = null,
        private var firstName: String? = null,
        private var email: String? = null,
        private var parentalEmail: String? = null,
        private var phoneNumber: String? = null,
        private var verificationToken: String? = null,
        private var verificationCode: String? = null,
        private var googleId: String? = null,
        private var googleIdToken: String? = null,
        private var googleAvatarUrl: String? = null,
        private var isBetaAccount: Boolean? = null,
        private var parentalConsentGiven: Boolean? = null,
        private var commercialSubscription: Boolean? = null,
        private var avatarUrl: String? = null
    ) : Parcelable {

        fun build(): CreateAccountData = CreateAccountData(
            birthDate,
            gender,
            handedness,
            country,
            appid,
            firstName,
            email,
            parentalEmail,
            phoneNumber,
            verificationToken,
            verificationCode,
            googleId,
            googleIdToken,
            googleAvatarUrl,
            isBetaAccount,
            parentalConsentGiven,
            commercialSubscription,
            avatarUrl
        )

        fun setBirthday(birthday: LocalDate): Builder =
            apply { this.birthDate = DATE_FORMATTER.format(birthday) }

        fun setGender(gender: Gender): Builder = apply {
            this.gender = gender.serializedName
        }

        fun setHandedness(handedness: Handedness): Builder = apply {
            this.handedness = handedness.serializedName
        }

        fun setCountry(country: String): Builder = apply {
            this.country = country
        }

        fun setAppid(appid: String): Builder = apply {
            this.appid = appid
        }

        fun setFirstName(firstName: String): Builder = apply {
            this.firstName = firstName
        }

        fun setEmail(email: String): Builder = apply {
            this.email = email
        }

        fun setParentalEmail(parentalEmail: String): Builder = apply {
            this.parentalEmail = parentalEmail
        }

        fun setPhoneNumber(phoneNumber: String): Builder = apply {
            this.phoneNumber = phoneNumber
        }

        fun setVerificationToken(verificationToken: String): Builder = apply {
            this.verificationToken = verificationToken
        }

        fun setGoogleId(googleId: String): Builder = apply {
            this.googleId = googleId
        }

        fun setGoogleIdToken(googleIdToken: String): Builder = apply {
            this.googleIdToken = googleIdToken
        }

        fun setGoogleAvatarUrl(googleAvatarUrl: String): Builder = apply {
            this.googleAvatarUrl = googleAvatarUrl
        }

        fun setVerificationCode(verificationCode: String): Builder = apply {
            this.verificationCode = verificationCode
        }

        fun setIsBetaAccount(isBetaAccount: Boolean): Builder = apply {
            this.isBetaAccount = isBetaAccount
        }

        fun setParentalConsentGiven(parentalConsentGiven: Boolean): Builder = apply {
            this.parentalConsentGiven = parentalConsentGiven
        }

        fun setCommercialSubscription(commercialSubscription: Boolean): Builder = apply {
            this.commercialSubscription = commercialSubscription
        }

        fun setAvatarUrl(avatarUrl: String): Builder = apply {
            this.avatarUrl = avatarUrl
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
