package com.kolibree.android.synchronizator.models

import androidx.annotation.Keep

@Keep
enum class UploadStatus {
    PENDING, IN_PROGRESS, COMPLETED, ERROR;

    fun stringify(): String = name

    companion object {

        @Throws(IllegalArgumentException::class)
        fun uploadStatusFromStringedValue(value: String): UploadStatus = valueOf(value)
    }
}
