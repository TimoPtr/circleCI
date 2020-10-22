/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.kolibree.android.auditor.Auditor
import com.kolibree.android.commons.ToothbrushModel
import timber.log.Timber

@Keep
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(TAG_LIFECYCLE).v("%s - onCreate", javaClass.simpleName)
    }

    override fun onStart() {
        super.onStart()
        Timber.tag(TAG_LIFECYCLE).v("%s - onStart", javaClass.simpleName)
    }

    override fun onResume() {
        super.onResume()
        Timber.tag(TAG_LIFECYCLE).v("%s - onResume", javaClass.simpleName)
    }

    override fun onPause() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onPause", javaClass.simpleName)
        super.onPause()
    }

    override fun onStop() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onStop", javaClass.simpleName)
        super.onStop()
    }

    override fun onDestroy() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onDestroy", javaClass.simpleName)
        super.onDestroy()
    }

    override fun setContentView(layoutResId: Int) {
        super.setContentView(layoutResId)

        ButterKnife.bind(this)
    }

    fun readMacFromIntent(): String? = intent.getStringExtra(INTENT_TOOTHBRUSH_MAC)

    fun readOptionalModelFromIntent(): ToothbrushModel? =
        intent.getSerializableExtra(INTENT_TOOTHBRUSH_MODEL) as? ToothbrushModel?

    fun readModelFromIntent(): ToothbrushModel = readOptionalModelFromIntent()
        ?: throw IllegalArgumentException(
            "Valid toothbrush model could not been found in intent bundle"
        )

    /*
    Prevent issue where we received "vibrating false" after registering as vibrator listener.
    We then considered this as the active connection, which is not what we want
    */
    fun shouldProceedWithVibration(): Boolean = !isDestroyed

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        Auditor.instance().notifyActivityGotTouchEvent(ev, this)
        return super.dispatchTouchEvent(ev)
    }

    protected fun setFullScreenDecorView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    companion object {
        const val INTENT_TOOTHBRUSH_MAC = "intentToothbrushMac"
        const val INTENT_TOOTHBRUSH_MODEL = "intentToothbrushModel"
        @JvmStatic
        protected val TAG_LIFECYCLE = "ActivityLifecycle"
    }
}
