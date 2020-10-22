/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.exception

import androidx.annotation.Keep

@Keep
class WeChatAccountNotRecognizedException(val loginAttemptToken: String) : Exception()
