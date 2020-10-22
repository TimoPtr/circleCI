/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import com.kolibree.android.app.ui.home.tab.profile.SETTINGS
import com.kolibree.android.tracker.Analytics

internal object SettingsProfileEventTracker {

    fun openGenderDialog() = sendEvent(GENDER)

    fun saveGender() = sendEvent(GENDER_SAVE)

    fun closeGenderDialog() = sendEvent(GENDER_GO_BACK)

    fun openHandednessDialog() = sendEvent(HANDEDNESS)

    fun setAge() = sendEvent(AGE_DONE)

    fun openAgeDialog() = sendEvent(AGE)

    fun closeAgeDialog() = sendEvent(AGE_GO_BACK)

    fun closeScreen() = sendEvent(GO_BACK)
}

internal object SettingsLogOutEventTracker {
    fun logOut() = sendEvent(LOG_OUT)

    fun logOutYes() = sendEvent(LOG_OUT_YES)

    fun logOutNo() = sendEvent(LOG_OUT_NO)
}

internal object SettingsDeleteAccountEventTracker {
    fun deleteAccount() = sendEvent(DELETE_ACCOUNT)

    fun deleteAccountYes() = sendEvent(DELETE_ACCOUNT_YES)

    fun deleteAccountNo() = sendEvent(DELETE_ACCOUNT_NO)
}

internal object SettingsFirstNameEventTracker {
    fun firstName() = sendEvent(FIRST_NAME)

    fun firstNameSave() = sendEvent(FIRST_NAME_SAVE)

    fun firstNameCancel() = sendEvent(FIRST_NAME_CANCEL)
}

internal object SettingsBrushTimerEventTracker {
    fun openBrushTimerDialog() = sendEvent(BRUSH_TIMER)

    fun setBrushTimer() = sendEvent(BRUSH_TIMER_SET)

    fun closeBrushTimerDialog() = sendEvent(BRUSH_TIMER_CANCEL)
}

private fun sendEvent(eventName: String) {
    Analytics.send(SETTINGS + eventName)
}

private const val GENDER = "Gender"
private const val GENDER_GO_BACK = "${GENDER}_GoBack"
private const val GENDER_SAVE = "${GENDER}_Save"
private const val HANDEDNESS = "Handedness"
private const val AGE = "Age"
private const val AGE_DONE = "${AGE}_Done"
private const val AGE_GO_BACK = "${AGE}_GoBack"
private const val GO_BACK = "GoBack"
private const val LOG_OUT = "LogOut"
private const val LOG_OUT_YES = "${LOG_OUT}_Yes"
private const val LOG_OUT_NO = "${LOG_OUT}_No"
private const val DELETE_ACCOUNT = "Account_DeleteAccount"
private const val DELETE_ACCOUNT_YES = "${DELETE_ACCOUNT}_Yes"
private const val DELETE_ACCOUNT_NO = "${DELETE_ACCOUNT}_No"
private const val FIRST_NAME = "FirstName"
private const val FIRST_NAME_SAVE = "${FIRST_NAME}_Save"
private const val FIRST_NAME_CANCEL = "${FIRST_NAME}_Cancel"
private const val BRUSH_TIMER = "BrushTimer"
private const val BRUSH_TIMER_SET = "${BRUSH_TIMER}_Set"
private const val BRUSH_TIMER_CANCEL = "${BRUSH_TIMER}_Cancel"
