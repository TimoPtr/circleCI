package com.kolibree.android.test

import androidx.annotation.Keep

/**
 * Exception to be used when forcing generic errors from test classes
 *
 * This'll allows to identify expected exceptions from unexpected ones
 */
@Keep
class TestForcedException : Exception("Test forced error")
