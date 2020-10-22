/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import androidx.annotation.Keep
import com.kolibree.account.Account
import com.kolibree.account.WeChatData
import com.kolibree.android.accountinternal.account.ParentalConsent
import com.kolibree.android.accountinternal.getAgeFromBirthDate
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.mocks.ProfileBuilder.CREATE_DATE_FORMATTER
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime

@Keep
fun createAccountInternal(
    id: Long = DEFAULT_TEST_ACCOUNT_ID,
    profiles: List<ProfileInternal> = listOf(createProfileInternal(accountId = id)),
    pubId: String = DEFAULT_ACCOUNT_PUB_ID,
    phoneNumber: String? = null,
    openId: String = "",
    unionId: String = "",
    accessToken: String = "",
    refreshToken: String = "",
    expiresIn: Int = 0,
    scope: String = "",
    email: String? = "hello@world.com",
    parentalConsent: ParentalConsent? = ParentalConsent.GRANTED,
    ownerProfileId: Long = profiles.firstOrNull { it.isOwnerProfile }?.id
        ?: ProfileBuilder.DEFAULT_ID
): AccountInternal {
    val account = AccountInternal(
        id = id,
        ownerProfileId = ownerProfileId,
        phoneNumber = phoneNumber,
        pubId = pubId,
        email = email,
        parentalConsent = parentalConsent,
        wcOpenId = openId,
        wcUnionId = unionId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        wcAccessToken = accessToken,
        wcRefreshToken = refreshToken,
        wcExpiresIn = expiresIn,
        wcScope = scope
    )

    account.internalProfiles = profiles
    account.setOwnerProfileAsCurrent()

    return account
}

@Keep
fun AccountInternal.toAccount(): Account {
    return Account(
        ownerProfileId = ownerProfileId,
        phoneNumber = phoneNumber,
        email = email,
        backendId = id,
        pubId = pubId,
        weChatData = extractWeChatData(this),
        profiles = internalProfiles.map { it.exportProfile() }
    )
}

private fun extractWeChatData(account: AccountInternal): WeChatData? {
    return if (account.wcOpenId == null) null else WeChatData(
        account.wcOpenId!!,
        account.wcUnionId!!,
        account.wcAccessToken!!,
        account.wcRefreshToken!!,
        account.wcExpiresIn!!,
        account.wcScope!!
    )
}

@Keep
fun createProfileInternalFromProfile(profile: Profile, accountId: Long): ProfileInternal {
    return ProfileInternal(
        id = profile.id,
        firstName = profile.firstName,
        points = profile.points,
        accountId = accountId.toInt(),
        brushingTime = profile.brushingGoalTime,
        creationDate = profile.createdDate,
        birthday = profile.birthday,
        exactBirthday = profile.exactBirthdate,
        age = profile.age
    )
}

@Keep
fun createProfileInternal(
    id: Long = ProfileBuilder.DEFAULT_ID,
    firstName: String = ProfileBuilder.DEFAULT_NAME,
    isOwnerProfile: Boolean = true,
    gender: String = ProfileBuilder.DEFAULT_GENDER.toString(),
    accountId: Long = DEFAULT_TEST_ACCOUNT_ID,
    brushingTime: Int = BrushingBuilder.DEFAULT_GOAL_DURATION,
    birthday: LocalDate = ProfileBuilder.DEFAULT_BIRTHDAY,
    creationDate: ZonedDateTime = TrustedClock.getNowZonedDateTime()
) = ProfileInternal(
    id = id,
    firstName = firstName,
    gender = gender,
    accountId = accountId.toInt(),
    birthday = birthday,
    age = getAgeFromBirthDate(birthday),
    brushingTime = brushingTime,
    creationDate = creationDate.format(CREATE_DATE_FORMATTER),
    isOwnerProfile = isOwnerProfile,
    brushingNumber = 0,
    points = 0
)

@Keep
fun createProfileInternal(profile: Profile, accountId: Int): ProfileInternal {
    return ProfileInternal(
        id = profile.id,
        firstName = profile.firstName,
        points = profile.points,
        accountId = accountId,
        brushingTime = profile.brushingGoalTime,
        creationDate = profile.createdDate,
        birthday = profile.birthday,
        exactBirthday = profile.exactBirthdate,
        age = profile.age,
        gender = profile.gender.serializedName,
        handedness = profile.handedness.serializedName,
        isOwnerProfile = !profile.deletable
    )
}
