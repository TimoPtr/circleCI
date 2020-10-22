package com.kolibree.sdkws.data.model.gopirate

import io.reactivex.Completable
import io.reactivex.Single

internal interface GoPirateDatastore {
    fun getData(profileId: Long): Single<GoPirateData>

    fun update(data: UpdateGoPirateData, profileId: Long): Completable

    fun update(goPirateData: GoPirateData, profileId: Long): Completable

    fun truncate()
}
