/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.addprofile

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.dialog.KolibreeBirthDatePickerDialog.Companion.earliestYear
import com.kolibree.android.app.ui.dialog.KolibreeBirthDatePickerDialog.Companion.latestYear
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

@Parcelize
internal data class AddProfileViewState(
    val name: String? = null,
    val birthday: String? = null,
    val gender: Gender? = null,
    val handedness: Handedness? = null,
    val termsAndConditionsAccepted: Boolean = false,
    val privacyPolicyAccepted: Boolean = false,
    val promotionsAndUpdatesAccepted: Boolean = false,
    val inputValidationActive: Boolean = false,
    val progressVisible: Boolean = false,
    val snackbarConfiguration: SnackbarConfiguration = SnackbarConfiguration(),
    val avatarUrl: String? = null
) : BaseViewState {

    fun bothConsentsNotAccepted(): Boolean = !termsAndConditionsAccepted && !privacyPolicyAccepted

    fun bothConsentsAccepted(): Boolean = termsAndConditionsAccepted && privacyPolicyAccepted

    fun withValidatedName(newName: String?): AddProfileViewState {
        return if (shouldTurnOnValidation(newName)) {
            copy(name = newName, inputValidationActive = true)
        } else copy(name = newName)
    }

    fun withNameValidation(): AddProfileViewState = copy(inputValidationActive = true)

    private fun shouldTurnOnValidation(newName: String?): Boolean =
        this.name.isValidName() && !newName.isValidName()

    fun isNameValid(): Boolean = name.isValidName()

    private fun String?.isValidName() = isNullOrEmpty().not()

    fun isBirthdayValid() = birthday.isBirthdayValid()

    private fun String?.isBirthdayValid(): Boolean {
        return this?.takeIf { it.isNotBlank() }?.let {
            try {
                val yearMonth = parsedBirthday()
                yearMonth != null && yearMonth.year in earliestYear()..latestYear()
            } catch (e: DateTimeParseException) {
                false
            }
        } ?: true
    }

    /**
     * birthday must be a valid MM/YYYY birthday
     */
    fun parsedBirthday() = birthday?.let { YearMonth.parse(it, MM_YYYY_FORMATTER) }

    fun withSnackbarDismissed(): AddProfileViewState =
        copy(snackbarConfiguration = snackbarConfiguration.copy(false))

    internal companion object {

        private const val MM_YYYY_PATTERN = "MM/yyyy"
        const val SLASH_POSITION_IN_DATE_PATTERN = 2

        @JvmField
        val MM_YYYY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(MM_YYYY_PATTERN)

        fun initial() = AddProfileViewState()
    }
}
