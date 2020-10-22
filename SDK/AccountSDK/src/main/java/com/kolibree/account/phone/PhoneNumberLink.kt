/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.account.phone

import androidx.annotation.Keep

/** Object used to link a verified phone number to an account */
@Keep
data class PhoneNumberLink internal constructor(
    val verificationToken: String,
    val phoneNumber: String
)
