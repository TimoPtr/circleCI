package com.kolibree.android.app.ui.fragment

import android.os.Bundle
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.app.ui.activity.BaseActivity
import com.kolibree.android.app.ui.activity.BaseGameActivity
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import io.reactivex.Single
import javax.inject.Inject

/**
 * Base class for game fragments. Provides all necessary callbacks to subclasses, by encapsulating [GameInteractor].
 *
 * @author lookashc
 */
@Deprecated(
    replaceWith = ReplaceWith("com.kolibree.android.game.mvi.BaseGameFragment"),
    message = "This Fragment is a pre-MVI one, please migrate to new architecture"
)
abstract class BaseGameFragment : BaseDaggerFragment(), GameInteractor.Listener {

    protected open val activity: BaseActivity?
        get() {
            validateActivity()
            return super.getActivity() as? BaseActivity
        }

    @Inject
    lateinit var gameInteractor: GameInteractor

    // unused but an unused inject is required to  make dagger generate the MembersInjector
    // in the base module, and it won't be generated in other modules any more.
    //
    // bug associated :  https://github.com/google/dagger/issues/955
    @Inject
    lateinit var serviceProvider: ServiceProvider

    protected val connection: KLTBConnection?
        get() = gameInteractor.connection

    val toothbrushMac: String?
        get() = gameInteractor.toothbrushMac

    val toothbrushMacSingle: Single<String>
        get() = gameInteractor.getToothbrushMacSingle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameInteractor.setLifecycleOwner(this)
        gameInteractor.toothbrushMacGetter = { activity?.readMacFromIntent() }
        gameInteractor.shouldProceedWithVibrationDelegate =
            { activity?.shouldProceedWithVibration() ?: false }
        gameInteractor.addListener(this)
    }

    override fun onDestroy() {
        gameInteractor.removeListener(this)
        super.onDestroy()
    }

    protected abstract fun setupLayout()

    override fun onKolibreeServiceConnected(service: KolibreeService) {
        setupLayout()
    }

    override fun onKolibreeServiceDisconnected() {
        // Reserved
    }

    override fun onConnectionEstablished() {
        // Reserved
    }

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        // Reserved
    }

    override fun onVibratorOn(connection: KLTBConnection) {
        // Reserved
    }

    override fun onVibratorOff(connection: KLTBConnection) {
        // Reserved
    }

    private fun validateActivity() {
        FailEarly.failInConditionMet(
            condition = super.getActivity() is BaseGameActivity,
            message = "Hosting BaseGameFragment in BaseGameActivity may cause duplication of logic, " +
                "please review your architecture!"
        ) {
            super.getActivity()?.finish()
        }
    }
}
