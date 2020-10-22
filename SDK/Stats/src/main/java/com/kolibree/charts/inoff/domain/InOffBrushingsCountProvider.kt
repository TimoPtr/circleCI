/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.inoff.domain

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.charts.inoff.data.persistence.InOffBrushingsCountDao
import com.kolibree.charts.inoff.data.persistence.model.InOffBrushingsCountEntity
import com.kolibree.charts.inoff.domain.model.InOffBrushingsCount
import io.reactivex.Flowable
import javax.inject.Inject

@VisibleForApp
interface InOffBrushingsCountProvider {

    fun brushingsCountStream(profileId: Long): Flowable<InOffBrushingsCount>
}

internal class InOffBrushingsCountProviderImpl @Inject constructor(
    private val dao: InOffBrushingsCountDao
) : InOffBrushingsCountProvider {

    override fun brushingsCountStream(profileId: Long): Flowable<InOffBrushingsCount> =
        dao.getByProfileStream(profileId).map(InOffBrushingsCountEntity::toInOffBrushingsCount)
}
