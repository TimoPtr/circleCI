package com.kolibree.android.sba.testbrushing.session

import android.os.Bundle
import android.view.View
import com.kolibree.android.app.mvi.brushstart.BrushStartFragment
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import com.kolibree.android.sba.testbrushing.brushing.creator.TestBrushingCreator
import com.kolibree.android.sba.testbrushing.tracker.TestBrushingEventTracker
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.tracker.Analytics
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import timber.log.Timber

internal class TestBrushingSessionFragment : BrushStartFragment() {

    @Inject
    lateinit var serviceProvider: ServiceProvider

    @Inject
    lateinit var navigator: TestBrushingNavigator

    @Inject
    lateinit var brushingCreator: TestBrushingCreator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Analytics.send(TestBrushingEventTracker.startScreen())
    }

    override fun onBrushStarted(model: ToothbrushModel, mac: String) {
        withConnectionForMac(mac) { connection ->
            brushingCreator.start(connection)
            navigator.navigateToDuringSessionScreen()
        }
    }

    private inline fun withConnectionForMac(
        mac: String,
        crossinline onSuccess: (KLTBConnection) -> Unit
    ) {
        disposeOnDestroy {
            serviceProvider.connectOnce()
                .map {
                    it.getConnection(mac) ?: throw IllegalStateException("No connection for $mac")
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ connection -> onSuccess(connection) }, Timber::e)
        }
    }
}
