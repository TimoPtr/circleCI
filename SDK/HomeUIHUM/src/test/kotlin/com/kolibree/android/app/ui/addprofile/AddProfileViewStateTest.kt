/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.addprofile

import io.kotlintest.shouldThrow
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeParseException

class AddProfileViewStateTest {

    @Test
    fun `isBirthdayValid checks whether birthday is valid or not`() {
        assertEquals(AddProfileViewState(birthday = "").isBirthdayValid(), true)
        assertEquals(AddProfileViewState(birthday = null).isBirthdayValid(), true)
        assertEquals(AddProfileViewState(birthday = "02/1960").isBirthdayValid(), true)
        assertEquals(AddProfileViewState(birthday = "2/1960").isBirthdayValid(), false)
        assertEquals(AddProfileViewState(birthday = "02/196").isBirthdayValid(), false)
        assertEquals(AddProfileViewState(birthday = "021960").isBirthdayValid(), false)
        assertEquals(AddProfileViewState(birthday = "021/960").isBirthdayValid(), false)
    }

    @Test
    fun `parsedBirthday when birthday string is valid returns a YearMonth`() {
        assertEquals(AddProfileViewState(birthday = "02/1960").parsedBirthday(), YearMonth.of(1960, 2))
    }

    @Test
    fun `parsedBirthday when birthday string is invalid throws DateTimeParseException`() {
        shouldThrow<DateTimeParseException> { AddProfileViewState(birthday = "02/200").parsedBirthday() }
        shouldThrow<DateTimeParseException> { AddProfileViewState(birthday = "2/2001").parsedBirthday() }
        shouldThrow<DateTimeParseException> { AddProfileViewState(birthday = "13/2001").parsedBirthday() }
        shouldThrow<DateTimeParseException> { AddProfileViewState(birthday = "022001").parsedBirthday() }
    }
}
