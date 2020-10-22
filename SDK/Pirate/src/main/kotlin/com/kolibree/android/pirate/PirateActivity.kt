package com.kolibree.android.pirate

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.ui.fragment.BaseUnityGameFragment
import com.kolibree.android.commons.ToothbrushModel

internal class PirateActivity : BasePirateCompatActivity() {
    override fun splashDrawable(): Int {
        TODO("Not yet implemented")
    }

    override fun splashText(): Int {
        TODO("Not yet implemented")
    }

    override fun showSomethingWentWrong(actionAfterAccept: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun unityGameFragment(): BaseUnityGameFragment {
        TODO("Not yet implemented")
    }
}

/**
 * TODO support Pirate in Hum/CC 3
 *
 * Create a pirate intent to use to open the game from another activity
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
): Intent = BasePirateCompatActivity.createPirateCompatIntent(
    context = context,
    hostActivity = PirateActivity::class.java,
    activityAfterGameFinish = activityClassToOpen,
    toothbrushModel = toothbrushModel,
    macAddress = macAddress
)
