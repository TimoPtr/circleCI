/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test

/**
 * Exception to be used when forcing generic errors from test classes
 *
 * This'll allows to identify expected exceptions from unexpected ones
 */
class RuntimeTestForcedException : RuntimeException("Test forced error")
