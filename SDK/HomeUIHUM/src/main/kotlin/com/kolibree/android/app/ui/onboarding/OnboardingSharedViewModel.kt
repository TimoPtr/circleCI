/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import androidx.lifecycle.LiveData
import com.kolibree.android.app.Error
import com.kolibree.android.app.ui.pairing.PairingFlowHost
import com.kolibree.sdkws.data.request.CreateAccountData
import io.reactivex.Completable

@Suppress("TooManyFunctions")
internal interface OnboardingSharedViewModel : PairingFlowHost {

    val sharedViewStateLiveData: LiveData<OnboardingSharedViewState>

    val nameValidationError: LiveData<Int>

    val emailValidationError: LiveData<Int>

    fun getSharedViewState(): OnboardingSharedViewState?

    fun showProgress(show: Boolean)

    override fun showError(error: Error)

    override fun hideError()

    fun enableOnScreenBackNavigation(enable: Boolean)

    fun getDataForAccountCreation(): CreateAccountData.Builder

    fun updateName(name: String?)

    fun enableNameValidation()

    fun updateEmail(newEmail: String?, isNewEmailValid: Boolean)

    fun enableEmailValidation()

    fun resetState()

    fun updatePromotionsAndUpdatesAccepted(accepted: Boolean)

    fun emailNewsletterSubscriptionCompletable(): Completable
}
