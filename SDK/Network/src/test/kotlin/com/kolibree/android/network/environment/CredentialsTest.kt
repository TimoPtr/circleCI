/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.network.environment

import junit.framework.TestCase.assertEquals
import org.junit.Test

class CredentialsTest {

    @Test
    fun `constructor trims parameters`() {
        val clientId = "end extra space "
        val secret = " start extra space"

        val credentials = Credentials(clientId, secret)

        assertEquals(clientId.trim(), credentials.clientId())
        assertEquals(secret.trim(), credentials.clientSecret())
    }

    @Test
    fun `constructor removes line breaks`() {
        val clientId = "\nline"
        val secret = "line\nextra"

        val credentials = Credentials(clientId, secret)

        assertEquals("line", credentials.clientId())
        assertEquals("lineextra", credentials.clientSecret())
    }
}
