/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.pairing.PairingViewState
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import kotlinx.android.parcel.Parcelize

@Suppress("TooManyFunctions")
@Parcelize
@VisibleForApp
data class OnboardingSharedViewState(
    val name: String? = null,
    private val nameValidationActive: Boolean = false,
    val email: String? = null,
    private val isEmailValid: Boolean = false,
    private val emailValidationActive: Boolean = false,
    val country: String,
    val isBetaAccount: Boolean,
    val screenHasBackNavigation: Boolean = false,
    private val progressVisible: Boolean = false,
    val promotionsAndUpdatesAccepted: Boolean = false,
    val pairingViewState: PairingViewState?,
    val snackbarConfiguration: SnackbarConfiguration = SnackbarConfiguration()
) : BaseViewState {

    fun progressVisible() = progressVisible || pairingViewState?.progressVisible ?: false

    fun withValidatedName(newName: String?): OnboardingSharedViewState {
        return if (shouldTurnOnValidation(newName)) {
            copy(name = newName, nameValidationActive = true)
        } else copy(name = newName)
    }

    fun withNameValidation(): OnboardingSharedViewState = copy(nameValidationActive = true)

    private fun shouldTurnOnValidation(newName: String?): Boolean =
        this.name.isValidName() && !newName.isValidName()

    fun nameValidationActive(): Boolean = nameValidationActive

    fun isNameValid(): Boolean = name.isValidName()

    fun withEmail(newEmail: String?, isNewEmailValid: Boolean): OnboardingSharedViewState {
        return if (emailChangedFromValidToNotValid(isNewEmailValid)) {
            copy(email = newEmail, isEmailValid = isNewEmailValid, emailValidationActive = true)
        } else copy(email = newEmail, isEmailValid = isNewEmailValid)
    }

    fun withEmailValidation(): OnboardingSharedViewState = copy(emailValidationActive = true)

    fun emailValidationActive(): Boolean = emailValidationActive

    fun isEmailValid(): Boolean = isEmailValid

    fun withSnackbarDismissed(): OnboardingSharedViewState =
        copy(snackbarConfiguration = snackbarConfiguration.copy(false))

    fun withPromotionsAndUpdatesAccepted(accepted: Boolean) = copy(promotionsAndUpdatesAccepted = accepted)

    private fun emailChangedFromValidToNotValid(isNewValueValid: Boolean): Boolean =
        this.isEmailValid && !isNewValueValid

    @VisibleForApp
    companion object {

        fun initial(country: String, isBetaAccount: Boolean) =
            OnboardingSharedViewState(
                country = country,
                isBetaAccount = isBetaAccount,
                pairingViewState = PairingViewState.initial()
            )
    }
}

private fun String?.isValidName() = isNullOrEmpty().not()
