package com.kolibree.android.sdk.connection.toothbrush

import android.content.Context
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
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
import com.kolibree.android.sdk.R
import javax.inject.Inject

class ToothbrushNameProvider @Inject constructor(
    private val context: Context
) {
    fun provide(model: ToothbrushModel): String {
        val nameRes = provideResource(model)
        return context.getString(nameRes)
    }

    @VisibleForTesting
    @StringRes
    internal fun provideResource(model: ToothbrushModel) = when (model) {
        ARA -> R.string.displayable_name_ara
        CONNECT_E1 -> R.string.displayable_name_e1
        CONNECT_E2 -> R.string.displayable_name_e2
        CONNECT_M1 -> R.string.displayable_name_m1
        CONNECT_B1 -> R.string.displayable_name_b1
        PLAQLESS -> R.string.displayable_name_pql
        HILINK -> R.string.displayable_name_hilink
        HUM_BATTERY -> R.string.displayable_name_hum_battery
        HUM_ELECTRIC -> R.string.displayable_name_hum_electric
        GLINT -> R.string.displayable_name_glint
    }
}
