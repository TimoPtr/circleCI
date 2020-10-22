package com.kolibree.android.accountinternal.account

import com.kolibree.android.network.models.RefreshTokenBody
import org.junit.Test

class RefreshTokenBodyTest {
    @Test(expected = IllegalArgumentException::class)
    fun emailNotNull_phoneNotNull_throwsException() {
        RefreshTokenBody("token", email = "em", phoneNumber = "ph")
    }

    @Test(expected = IllegalArgumentException::class)
    fun emailNull_phoneNull_throwsException() {
        RefreshTokenBody("token", email = null, phoneNumber = null)
    }

    @Test
    fun emailNull_phoneNotNull_doesNothing() {
        RefreshTokenBody("token", email = null, phoneNumber = "ph")
    }

    @Test
    fun emailNotNull_phoneNull_doesNothing() {
        RefreshTokenBody("token", email = "em", phoneNumber = null)
    }
}
