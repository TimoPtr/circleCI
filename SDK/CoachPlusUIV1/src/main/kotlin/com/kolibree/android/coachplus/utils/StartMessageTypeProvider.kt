/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.coachplus.utils

import androidx.annotation.Keep
import com.kolibree.android.coachplus.R
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.HUM_BATTERY
import com.kolibree.android.commons.ToothbrushModel.HUM_ELECTRIC
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sdk.connection.KLTBConnection
import java.util.HashSet
import javax.inject.Inject

@Keep
sealed class StartMessageType

@Keep
class OneModel(val modelComercialName: String) : StartMessageType()

@Keep
object MultipleModels : StartMessageType()

@Keep
object NoModel : StartMessageType()

@Keep
class StartMessageTypeProvider @Inject constructor() {

    fun provideType(connections: List<KLTBConnection>): StartMessageType {
        val models = connections.map {
            it.toothbrush().model
        }

        val uniqueModels = HashSet(models)
        return when {
            uniqueModels.size == 0 -> NoModel
            uniqueModels.size == 1 -> {
                val model = models.first()
                OneModel(model.commercialName)
            }
            else -> MultipleModels
        }
    }

    fun modelResource(model: ToothbrushModel) = when (model) {
        ARA -> R.raw.anim_step2_ara
        CONNECT_E1 -> R.raw.anim_step2_e1
        CONNECT_E2 -> R.raw.anim_step2_e2
        CONNECT_M1 -> R.raw.anim_m1
        CONNECT_B1 -> R.raw.anim_step2_b1
        PLAQLESS -> R.raw.anim_step2_pql
        HILINK -> R.raw.anim_step2_e2
        HUM_ELECTRIC -> R.raw.anim_step2_e2
        HUM_BATTERY -> R.raw.anim_step2_b1
        GLINT -> R.raw.anim_step2_e2
    }

    fun descriptionResource(model: ToothbrushModel) = if (model.isManual) {
        R.string.coach_start_message_title_manual
    } else {
        R.string.coach_start_message_title
    }
}
