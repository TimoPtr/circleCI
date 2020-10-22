/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.network.environment

import androidx.annotation.Keep

/** Created by miguelaragues on 13/3/18.  */
@Keep
data class Credentials(val clientId: String, val clientSecret: String) {

    private val clientIdSanitized: String = sanitizeParameter(clientId)
    private val clientSecretSanitized: String = sanitizeParameter(clientSecret)

    private fun sanitizeParameter(clientId: String): String =
        clientId.trim().replace("\n", "")

    fun clientId(): String = clientIdSanitized

    fun clientSecret(): String = clientSecretSanitized

    override fun toString(): String = "Don't log credentials in any case, use debug instead"

    fun validateCredentials(): Boolean =
        clientIdSanitized.isNotBlank() && clientSecretSanitized.isNotBlank()
}
