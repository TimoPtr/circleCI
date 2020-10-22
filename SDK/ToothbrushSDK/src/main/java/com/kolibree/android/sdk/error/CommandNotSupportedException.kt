package com.kolibree.android.sdk.error

/** FailureReason that is thrown when a toothbrush firmware version doesn't implement a command */
class CommandNotSupportedException(
    message: String = "This feature is not available on this device"
) : FailureReason(message)
