/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushpicker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.kolibree.android.app.ui.dialog.DaggerDialogFragment
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.utils.DisposableScope
import com.kolibree.databinding.OnItemClickListener
import com.kolibree.game.legacy.R
import com.kolibree.game.legacy.databinding.FragmentToothbrushPickerBinding
import com.kolibree.sdkws.core.InternalKolibreeConnector
import javax.inject.Inject

@Keep
@SuppressLint("ExperimentalClassUse")
class ToothbrushPickerDialogFragment : DaggerDialogFragment<FragmentToothbrushPickerBinding>() {

    override val dialogLayoutId: Int = R.layout.fragment_toothbrush_picker

    private var onToothbrushChosenCallback: ((AccountToothbrush) -> Unit)? = null

    @Inject
    lateinit var toothbrushRepository: ToothbrushRepository

    @Inject
    lateinit var kolibreeConnector: InternalKolibreeConnector

    private val disposedOnDestroy = DisposableScope("onDestroy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposedOnDestroy.ready()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeButton.setOnClickListener { dismiss() }
        retrieveConnections()
    }

    override fun onDestroy() {
        disposedOnDestroy.dispose()
        super.onDestroy()
    }

    private fun retrieveConnections() {
        val toothbrushes = toothbrushRepository.getAccountToothbrushes(kolibreeConnector.accountId)
        bindToothbrushes(toothbrushes)
    }

    private fun bindToothbrushes(toothbrushes: List<AccountToothbrush>) {
        binding.toothbrushes = toothbrushes
        binding.onClickListener = object : OnItemClickListener<AccountToothbrush>() {
            override fun onItemClick(item: AccountToothbrush) {
                onToothbrushChosenCallback?.invoke(item)
                dismiss()
            }
        }
    }

    companion object {

        private val TAG: String = ToothbrushPickerDialogFragment::class.java.name

        @JvmStatic
        fun show(fragmentManager: FragmentManager?, callback: ((AccountToothbrush) -> Unit)) {
            fragmentManager?.let {
                if (it.findFragmentByTag(TAG) == null) {
                    val dialog = ToothbrushPickerDialogFragment()
                    dialog.onToothbrushChosenCallback = callback
                    dialog.showNow(it, TAG)
                }
            }
        }

        @JvmStatic
        fun hide(fragmentManager: FragmentManager?) {
            val dialog = fragmentManager?.findFragmentByTag(TAG) as DialogFragment?
            dialog?.dismissAllowingStateLoss()
        }
    }
}
