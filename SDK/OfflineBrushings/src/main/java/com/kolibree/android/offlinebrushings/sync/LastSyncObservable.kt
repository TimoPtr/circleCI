package com.kolibree.android.offlinebrushings.sync

import io.reactivex.Observable

interface LastSyncObservable {
    fun observable(): Observable<LastSyncData>
    fun getLastSyncData(tbMac: String): LastSyncData
}
