package com.kolibree.android.sba.testbrushing.results

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.kolibree.android.app.ui.dialog.SimpleRoundedDialog
import com.kolibree.android.sba.R

internal class ReadDiagramDialog : SimpleRoundedDialog() {

    companion object {
        val TAG = ReadDiagramDialog::class.java.name

        fun show(fragmentManager: FragmentManager?) {
            fragmentManager?.let {
                if (it.findFragmentByTag(TAG) == null) {
                    val dialog = ReadDiagramDialog()
                    dialog.isCancelable = true
                    dialog.showNow(it, TAG)
                }
            }
        }

        fun hide(fragmentManager: FragmentManager?) {
            val dialog = fragmentManager?.findFragmentByTag(ReadDiagramDialog.TAG) as DialogFragment?
            dialog?.dismissAllowingStateLoss()
        }
    }

    override fun layoutId() = R.layout.dialog_read_diagram

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.read_diagram_ok).setOnClickListener { dismissAllowingStateLoss() }
    }
}
