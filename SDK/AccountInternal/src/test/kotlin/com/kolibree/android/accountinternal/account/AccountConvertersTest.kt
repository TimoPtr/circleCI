package com.kolibree.android.accountinternal.account

import com.kolibree.android.accountinternal.account.ParentalConsent.GRANTED
import com.kolibree.android.accountinternal.account.ParentalConsent.PENDING
import com.kolibree.android.accountinternal.account.ParentalConsent.UNKNOWN
import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountConvertersTest : BaseUnitTest() {

    private val accountConverters = AccountConverters()

    /*
    TO PARENTAL CONSENT
     */
    @Test
    fun toParentalConsent_nullInput_outputsNull() {
        assertNull(accountConverters.toParentalConsent(null))
    }

    @Test
    fun toParentalConsent_UNKNOWNInput_outputsNull() {
        assertNull(accountConverters.toParentalConsent(UNKNOWN))
    }

    @Test
    fun toParentalConsent_PENDINGInput_outputs0() {
        assertEquals(0, accountConverters.toParentalConsent(PENDING))
    }

    @Test
    fun toParentalConsent_GRANTEDInput_outputs1() {
        assertEquals(1, accountConverters.toParentalConsent(GRANTED))
    }

    /*
    FROM PARENTAL CONSENT
     */
    @Test
    fun fromParentalConsent_0_PENDING() {
        assertEquals(ParentalConsent.PENDING, accountConverters.fromParentalConsent(0))
    }

    @Test
    fun fromParentalConsent_1_GRANTED() {
        assertEquals(ParentalConsent.GRANTED, accountConverters.fromParentalConsent(1))
    }

    @Test
    fun fromParentalConsent_2_UNKNOWN() {
        assertEquals(ParentalConsent.UNKNOWN, accountConverters.fromParentalConsent(2))
    }

    @Test
    fun fromParentalConsent_minus1_UNKNOWN() {
        assertEquals(ParentalConsent.UNKNOWN, accountConverters.fromParentalConsent(-1))
    }
}
