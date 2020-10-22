/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.SHORT_MONTH_FORMAT
import java.util.Locale
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.threeten.bp.format.DateTimeFormatter

internal class BirthDateDataBuilderTest : BaseUnitTest() {

    @Test
    fun `buildYears() creates the correct years`() {
        val expected = arrayOf("2000", "2001", "2002", "2003", "2004", "2005")
        val years = buildYears(2000, 2005)
        assertThat(years, equalTo(expected))
    }

    @Test
    fun `buildMonths() creates the correct months in English`() {
        val expected = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val years = buildMonths(DateTimeFormatter.ofPattern(SHORT_MONTH_FORMAT, Locale.ENGLISH))
        assertThat(years, equalTo(expected))
    }

    @Test
    fun `buildMonths() creates the correct months in French`() {
        val expected = arrayOf(
            "janv.",
            "févr.",
            "mars",
            "avr.",
            "mai",
            "juin",
            "juil.",
            "août",
            "sept.",
            "oct.",
            "nov.",
            "déc."
        )
        val years = buildMonths(DateTimeFormatter.ofPattern(SHORT_MONTH_FORMAT, Locale.FRENCH))
        assertThat(years, equalTo(expected))
    }
}
