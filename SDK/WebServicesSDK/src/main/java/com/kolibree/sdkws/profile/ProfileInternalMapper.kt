package com.kolibree.sdkws.profile

import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.failearly.FailEarly
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.data.model.EditProfileData.UNSET
import javax.inject.Inject

internal class ProfileInternalMapper @Inject constructor() {

    fun copyEditData(profileInternal: ProfileInternal, data: EditProfileData): ProfileInternal =
        with(profileInternal) {
            var newBrushingTime = if (data.brushingTime != UNSET) data.brushingTime else brushingTime
            FailEarly.failInConditionMet(
                newBrushingTime == UNSET,
                message = "We may save UNSET brushing time here!"
            ) {
                newBrushingTime = DEFAULT_BRUSHING_GOAL
            }
            return@with copy(
                firstName = data.firstName ?: firstName,
                gender = data.gender ?: gender,
                handedness = data.handedness ?: handedness,
                addressCountry = data.countryCode ?: addressCountry,
                age = if (data.age != UNSET) data.age else age,
                brushingTime = newBrushingTime,
                brushingNumber = if (data.brushingNumber != UNSET) data.brushingNumber else brushingNumber
            )
        }
}
