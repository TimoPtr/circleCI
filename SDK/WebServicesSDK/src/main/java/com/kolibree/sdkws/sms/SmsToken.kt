package com.kolibree.sdkws.sms

import androidx.annotation.Keep

@Keep
data class SmsToken(val phoneNumber: String = "", val verificationToken: String = "")
