package com.kolibree.android.app.toothbrush

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.values

internal object FlavorToothbrushModels {
    fun defaultSupportedModels(): Set<ToothbrushModel> {
        return values().filter { !it.isHumToothbrush }.toSet()
    }
}
