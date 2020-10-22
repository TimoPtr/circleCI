/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.profile.persistence

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.failearly.FailEarly
import java.lang.reflect.Type

@Keep
class ProfileInternalAdapter(private val gson: Gson) : JsonDeserializer<ProfileInternal> {

    @Throws(JsonParseException::class)
    override fun deserialize(je: JsonElement, type: Type, jdc: JsonDeserializationContext): ProfileInternal {
        // Get the "content" element from the parsed JSON
        val profileJson = je.asJsonObject
        val pictureGetUrl = profileJson.get(ProfileInternal.FIELD_PICTURE_GET_URL)?.asString
        val pictureUploadUrl = profileJson.get(ProfileInternal.FIELD_PICTURE_UPLOAD_URL)?.asString
        val points = je.asJsonObject.get(ProfileInternal.FIELD_STATS).asJsonObject
            .get(ProfileInternal.FIELD_POINTS).asInt

        val profile = gson.fromJson(profileJson, ProfileInternal::class.java)
        FailEarly.failInConditionMet(profile.brushingTime == -1, message = "Backend sent us brushingTime == -1!") {
            profile.brushingTime = DEFAULT_BRUSHING_GOAL
        }
        profile.points = points

        // small hack to avoid adding those fields to room and then create a migration for it.
        profile.pictureGetUrl = pictureGetUrl
        profile.pictureUploadUrl = pictureUploadUrl

        return profile
    }
}
