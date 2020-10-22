/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMoment
import com.kolibree.android.headspace.mindful.ui.HeadspaceMindfulMomentScreenResult.Closed
import com.kolibree.android.headspace.mindful.ui.HeadspaceMindfulMomentScreenResult.Collected
import org.threeten.bp.OffsetDateTime

@VisibleForApp
class HeadspaceMindfulMomentScreenContract :
    ActivityResultContract<HeadspaceMindfulMoment, HeadspaceMindfulMomentScreenResult>() {

    override fun createIntent(context: Context, input: HeadspaceMindfulMoment) =
        createHeadspaceMindfulMomentIntent(context, input)

    override fun parseResult(resultCode: Int, intent: Intent?): HeadspaceMindfulMomentScreenResult =
        when (resultCode) {
            RESULT_OK -> Collected(
                intent?.getSerializableExtra(EXTRA_COLLECTED_TIME) as OffsetDateTime
            )
            else -> Closed
        }
}

@VisibleForApp
sealed class HeadspaceMindfulMomentScreenResult {
    @VisibleForApp
    class Collected(val collectionTime: OffsetDateTime) : HeadspaceMindfulMomentScreenResult()

    @VisibleForApp
    object Closed : HeadspaceMindfulMomentScreenResult()
}
