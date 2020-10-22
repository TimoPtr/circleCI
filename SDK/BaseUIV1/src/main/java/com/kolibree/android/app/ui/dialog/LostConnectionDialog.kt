/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.kolibree.android.baseui.v1.R
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import timber.log.Timber

@SuppressLint("DeobfuscatedPublicSdkClass")
class LostConnectionDialog : SimpleRoundedDialog(persistentDialog = true), HasAndroidInjector {

    @Inject
    internal lateinit var childFragmentInjector: DispatchingAndroidInjector<Any>

    @Inject
    internal lateinit var progressIndicatorAnimation: Animation

    private lateinit var dismissCallback: () -> Unit

    override fun layoutId() = R.layout.dialog_lost_connection

    private lateinit var ivIndication: ImageView
    private lateinit var tvQuit: TextView

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        super.onCreateView(inflater, container, savedInstanceState)?.let { root ->
            initViews(root)
            root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        tvQuit.setOnClickListener {
            dismissAllowingStateLoss()
            dismissCallback()
        }
    }

    private fun initViews(root: View) {
        ivIndication = root.findViewById(R.id.ivIndication)
        tvQuit = root.findViewById(R.id.tvQuit)
    }

    override fun onResume() {
        super.onResume()
        processArguments()
    }

    override fun androidInjector(): AndroidInjector<Any> = childFragmentInjector

    private fun processArguments() {
        val isConnecting = arguments?.getBoolean(IS_CONNECTING) ?: false
        if (isConnecting) {
            connectionInProgress()
        } else {
            noConnection()
        }
    }

    private fun noConnection() {
        ivIndication.setImageResource(R.drawable.rounded_red)
        ivIndication.startAnimation(progressIndicatorAnimation)
    }

    private fun connectionInProgress() {
        ivIndication.setImageResource(R.drawable.rounded_orange)
        ivIndication.startAnimation(progressIndicatorAnimation)
    }

    companion object {

        const val FADE_IN_DURATION = 500L
        private const val IS_CONNECTING = "is_connecting"
        private val TAG = LostConnectionDialog::class.java.name

        private fun show(
            fragmentManager: FragmentManager?,
            isConnecting: Boolean,
            dismissCallback: () -> Unit
        ) {
            fragmentManager?.let {
                val args = Bundle()
                args.putBoolean(IS_CONNECTING, isConnecting)

                if (it.findFragmentByTag(TAG) == null) {
                    Timber.d("Showing new dialog")
                    val dialog = LostConnectionDialog()
                    dialog.arguments = args
                    dialog.dismissCallback = dismissCallback
                    dialog.showNow(it, TAG)
                } else {
                    Timber.d("Reusing previous dialog")
                    val dialog = it.findFragmentByTag(TAG) as LostConnectionDialog
                    dialog.arguments = args
                    dialog.processArguments()
                }
            }
        }

        private fun hide(fragmentManager: FragmentManager?) {
            val dialog = fragmentManager?.findFragmentByTag(TAG) as DialogFragment?
            dialog?.dismissAllowingStateLoss()
        }

        @JvmStatic
        fun update(
            fragmentManager: FragmentManager?,
            state: State,
            dismissCallback: () -> Unit
        ) {
            when (state) {
                State.CONNECTING -> show(fragmentManager, true, dismissCallback)
                State.CONNECTION_LOST -> show(fragmentManager, false, dismissCallback)
                State.CONNECTION_ACTIVE -> hide(fragmentManager)
            }
        }
    }
}
