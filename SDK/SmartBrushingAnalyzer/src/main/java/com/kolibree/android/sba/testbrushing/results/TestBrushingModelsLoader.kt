package com.kolibree.android.sba.testbrushing.results

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.jaws.Kolibree3DModel
import com.kolibree.android.jaws.MemoryManager
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class TestBrushingModelsLoader
@Inject constructor(private val memoryManager: MemoryManager) : DefaultLifecycleObserver {

    @VisibleForTesting
    var disposable: Disposable? = null

    override fun onCreate(owner: LifecycleOwner) {
        val preloadUpperJaw = memoryManager.preloadFromAssets(Kolibree3DModel.UPPER_JAW)
        val preloadLowerJaw = memoryManager.preloadFromAssets(Kolibree3DModel.LOWER_JAW)
        disposable = preloadUpperJaw.andThen(preloadLowerJaw)
            .subscribeOn(Schedulers.computation())
            .subscribe({}, Timber::e)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        disposable.forceDispose()
    }
}
