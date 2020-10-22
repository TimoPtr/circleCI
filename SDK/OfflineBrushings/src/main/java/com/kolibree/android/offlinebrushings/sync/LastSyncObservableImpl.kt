package com.kolibree.android.offlinebrushings.sync

import com.jakewharton.rx.ReplayingShare
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import javax.inject.Inject

internal class LastSyncObservableImpl
@Inject constructor(private val lastSyncProvider: LastSyncProvider) :
    LastSyncObservable, LastSyncObservableInternal {

    private val publishRelay = PublishRelay.create<LastSyncData>()

    private var observable: Observable<LastSyncData> =
        publishRelay
            .compose(ReplayingShare.instance())

    override fun observable() = observable

    override fun send(data: LastSyncData) {
        publishRelay.accept(data)
        when (data) {
            is LastSyncDate -> {
                lastSyncProvider.put(data.tbMac, data.date)
            }
        }
    }

    override fun getLastSyncData(tbMac: String) = lastSyncProvider.get(tbMac)
}
