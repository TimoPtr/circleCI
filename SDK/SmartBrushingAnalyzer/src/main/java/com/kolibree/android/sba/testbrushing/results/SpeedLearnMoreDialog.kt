package com.kolibree.android.sba.testbrushing.results

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.kolibree.android.app.extensions.setUnderlineText
import com.kolibree.android.app.ui.dialog.SimpleRoundedDialog
import com.kolibree.android.sba.R

internal class SpeedLearnMoreDialog : SimpleRoundedDialog() {

    companion object {
        val TAG = SpeedLearnMoreDialog::class.java.name

        fun show(fragmentManager: FragmentManager?, callback: () -> Unit) {
            fragmentManager?.let {
                if (it.findFragmentByTag(TAG) == null) {
                    val dialog = SpeedLearnMoreDialog()
                    dialog.isCancelable = true
                    dialog.howReadDiagramCallback = callback
                    dialog.showNow(it, TAG)
                }
            }
        }

        fun hide(fragmentManager: FragmentManager?) {
            val dialog = fragmentManager?.findFragmentByTag(TAG) as DialogFragment?
            dialog?.dismissAllowingStateLoss()
        }
    }

    private var howReadDiagramCallback = {}

    override fun layoutId() = R.layout.fragment_speed_learn_more_dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val needHelpText = getString(R.string.speed_learn_more_dialog_help)
        view.findViewById<TextView>(R.id.speed_learn_more_help)
            .setUnderlineText(needHelpText, R.color.colorPrimaryDark)

        view.findViewById<View>(R.id.speed_learn_more_ok).setOnClickListener {
            dismissAllowingStateLoss()
        }
        view.findViewById<View>(R.id.speed_learn_more_help).setOnClickListener {
            howReadDiagramCallback.invoke()
            dismissAllowingStateLoss()
        }
    }
}
