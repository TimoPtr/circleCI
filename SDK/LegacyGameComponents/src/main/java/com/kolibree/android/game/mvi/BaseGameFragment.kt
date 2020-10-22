/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.mvi

import androidx.annotation.Keep
import androidx.databinding.ViewDataBinding
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dialog.FinishBrushingDialog
import com.kolibree.android.app.ui.dialog.LostConnectionDialog
import com.kolibree.android.auditor.UserStep

/**
 * Generic game MVI fragment, with lost connection support and vibration detection.
 */
@Keep
abstract class BaseGameFragment<
    VS : BaseGameViewState,
    VMF : BaseViewModel.Factory<VS>,
    VM : BaseGameViewModel<VS>,
    B : ViewDataBinding> :
    BaseMVIFragment<VS, BaseGameAction, VMF, VM, B>(), UserStep {

    /**
     * If the action is handled by the derived implementation, return true at the end.
     * This way action will be marked as consumed and won't be processed further.
     *
     * @param action action to execute and - possibly - consume
     * @return true if the event is consumed, otherwise - false
     */
    abstract fun executeAndConsume(action: BaseGameAction): Boolean

    final override fun execute(action: BaseGameAction) {
        if (executeAndConsume(action)) return

        when (action) {
            is ConnectionHandlerStateChanged -> {
                LostConnectionDialog.update(
                    childFragmentManager,
                    action.state,
                    dismissCallback = { activity?.finish() }
                )
            }
            is VibratorStateChanged -> {
                if (action.isOn) {
                    FinishBrushingDialog.hide(childFragmentManager)
                } else {
                    FinishBrushingDialog.show(
                        childFragmentManager,
                        answerCallback = { finishGame ->
                            if (finishGame) activity?.finish()
                            else viewModel.resumeGame()
                        }
                    )
                }
            }
        }
    }
}
