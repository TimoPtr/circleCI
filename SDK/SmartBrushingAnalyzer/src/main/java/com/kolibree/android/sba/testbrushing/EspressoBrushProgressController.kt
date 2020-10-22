package com.kolibree.android.sba.testbrushing

import android.annotation.SuppressLint
import com.kolibree.android.sba.testbrushing.progress.TestBrushProgressController
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Do not used this class outside of Espresso test
 * There is an issue with kapt when this class is in the AndroidTest folder
 * https://stackoverflow.com/questions/54326219/dagger-android-not-generating-components-under-test-folder
 *  TODO : check the SO link from time to time to check if there is any anwer
 */
@SuppressLint("DeobfuscatedPublicSdkClass", "VisibleForTests")
class EspressoBrushProgressController : TestBrushProgressController {

    private val publishSubject = PublishSubject.create<Long>()

    override fun controllerObservable(): Observable<Long> {
        return publishSubject
    }

    fun emitTick(tick: Int) {
        publishSubject.onNext(tick.toLong())
    }
}
