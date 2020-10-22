package com.kolibree.sdkws.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import org.threeten.bp.LocalDate

/** Created by aurelien on 28/09/15.  */
@Keep
class CreateProfileData {
    @SerializedName("first_name")
    var firstName: String? = null

    @SerializedName("gender")
    var gender: String? = null
        private set

    @SerializedName("survey_handedness")
    var handedness: String? = null
        private set

    @SerializedName("address_country")
    var country: String? = null
    var picturePath: String? = null

    @SerializedName("age")
    var age = 0

    @SerializedName("birthday")
    var birthday: LocalDate? = null

    @SerializedName("brushing_goal_time")
    private val brushingTime = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS

    fun setGender(gender: Gender) {
        this.gender = gender.serializedName
    }

    fun setHandedness(handedness: Handedness) {
        this.handedness = handedness.serializedName
    }

    fun hasPicture(): Boolean {
        return picturePath != null
    }
}
