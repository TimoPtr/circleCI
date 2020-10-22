package com.kolibree.android.offlinebrushings.persistence

import androidx.annotation.Keep
import com.kolibree.android.offlinebrushings.OrphanBrushing
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Interface to be exposed to 3rd party SDK consumers
 */
@Keep
interface SDKOrphanBrushingRepository {

    fun count(): Flowable<Int>

    fun readAll(): Flowable<List<OrphanBrushing>>

    fun read(id: Long): Single<OrphanBrushing>
}
