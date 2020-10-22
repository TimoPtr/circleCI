/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailVerifierTest {

    @Test
    fun isValid_validEmail_returnsTrue() {
        val verifier = EmailVerifierImpl()
        assertTrue(verifier.isValid(VALID_EMAIL))
    }

    @Test
    fun isValid_invalidEmail_returnsFalse() {
        val verifier = EmailVerifierImpl()
        assertFalse(verifier.isValid("emal"))
        assertFalse(verifier.isValid("email@"))
        assertFalse(verifier.isValid("email@email"))
        assertFalse(verifier.isValid("@email.com"))
    }

    @Test
    fun isEmpty_emptyEmail_returnsTrue() {
        val verifier = EmailVerifierImpl()
        assertTrue(verifier.isEmpty(""))
        assertTrue(verifier.isEmpty(null))
    }

    @Test
    fun isEmpty_notEmptyEmail_returnsFalse() {
        val verifier = EmailVerifierImpl()
        assertFalse(verifier.isEmpty(VALID_EMAIL))
        assertFalse(verifier.isEmpty("invalid@email"))
    }

    @Test
    fun isMatchingEmailPattern_invalidEmail_returnsFalse() {
        val verifier = EmailVerifierImpl()
        assertFalse(verifier.isMatchingEmailPattern(""))
        assertFalse(verifier.isMatchingEmailPattern("emal"))
        assertFalse(verifier.isValid("email@"))
        assertFalse(verifier.isValid("email@email"))
        assertFalse(verifier.isValid("@email.com"))
    }

    @Test
    fun isMatchingEmailPattern_validEmail_returnsTrue() {
        val verifier = EmailVerifierImpl()
        assertTrue(verifier.isMatchingEmailPattern(VALID_EMAIL))
        assertTrue(verifier.isMatchingEmailPattern("emal@valid.com"))
    }

    companion object {
        private const val VALID_EMAIL = "kornel@kolibree.com"
    }
}
