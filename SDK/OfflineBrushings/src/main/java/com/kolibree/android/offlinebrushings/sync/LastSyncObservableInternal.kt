package com.kolibree.android.offlinebrushings.sync

internal interface LastSyncObservableInternal : LastSyncObservable {
    fun send(data: LastSyncData)
}
