package com.kolibree.android.sba.testbrushing.progress

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import io.reactivex.Observable

@SuppressLint("DeobfuscatedPublicSdkClass")
@VisibleForTesting(otherwise = PACKAGE_PRIVATE)
interface TestBrushProgressController {
    fun controllerObservable(): Observable<Long>
}
