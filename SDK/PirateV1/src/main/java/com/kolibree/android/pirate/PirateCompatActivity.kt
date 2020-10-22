/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.ui.dialog.PopupDialogFragment
import com.kolibree.android.app.ui.fragment.BaseUnityGameFragment
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.pirate.BasePirateCompatActivity.Companion.createPirateCompatIntent

/*
Don't remove this Keep. Important for SDK
 */
@Keep
internal class PirateCompatActivity : BasePirateCompatActivity(),
    PopupDialogFragment.PopupClosedListener {

    private var actionAfterSomethingWentWrong: (() -> Unit)? = null

    override fun splashDrawable(): Int = R.drawable.pirate_splash_screen

    override fun splashText(): Int = R.string.please_wait

    override fun unityGameFragment(): BaseUnityGameFragment = createPirateFragment(
        readModelFromIntent(),
        readMacFromIntent()
            ?: throw IllegalArgumentException("MAC address should be pass to the activity")
    )

    override fun showSomethingWentWrong(actionAfterAccept: () -> Unit) {
        if (actionAfterSomethingWentWrong == null) {
            actionAfterSomethingWentWrong = actionAfterAccept

            showSomethingWentWrong(SOMETHING_WRONG_POPUP_ID)
        }
    }

    private fun showSomethingWentWrong(popupId: Int) {
        val popup = PopupDialogFragment.newInstance(
            getString(R.string.error),
            getString(R.string.error_no_internet),
            popupId
        )
        popup.isCancelable = false
        popup.showNow(supportFragmentManager, null)
    }

    override fun onPopupClosed(popupId: Int) {
        actionAfterSomethingWentWrong?.invoke().also {
            actionAfterSomethingWentWrong = null
        }
    }
}

/**
 * Create a pirate intent to use to open the game from another activity
 *
 * The returned Intent will start a new Task. Thus, [AppCompatActivity.startActivityForResult] will
 * not work on this Intent
 *
 * @param context current context
 * @param toothbrushModel model of the toothbrush that will be used
 * @param macAddress of the toothbrush that will be used
 * @param activityClassToOpen activity's class to open when finishing the game
 * @return pirate game intent
 */
@Keep
fun <T : AppCompatActivity> createPirateIntent(
    context: Context,
    toothbrushModel: ToothbrushModel,
    macAddress: String,
    activityClassToOpen: Class<T>
): Intent = createPirateCompatIntent(
    context = context,
    hostActivity = PirateCompatActivity::class.java,
    activityAfterGameFinish = activityClassToOpen,
    toothbrushModel = toothbrushModel,
    macAddress = macAddress
)

private const val SOMETHING_WRONG_POPUP_ID = 12374
