package com.kolibree.android.sdk.core.ota.kltb003

import androidx.annotation.Keep

@Keep
class RecoverableDfuException(errorMessage: String, errorCode: Int) : Exception("$errorMessage $errorCode")
