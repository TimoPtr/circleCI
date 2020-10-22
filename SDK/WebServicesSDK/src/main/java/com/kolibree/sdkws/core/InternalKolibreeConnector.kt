package com.kolibree.sdkws.core

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.exception.NoAccountException
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.kolibree.sdkws.data.model.Practitioner
import com.kolibree.sdkws.data.model.gopirate.UpdateGoPirateData
import io.reactivex.Single

/**
 * Interface for internal use only
 *
 *
 * Provides access to some methods that we don't want to expose publicly
 *
 *
 * Created by miguelaragues on 6/3/18.
 */
@Keep
interface InternalKolibreeConnector : IKolibreeConnector {
    fun getRemoteAccount(accountId: Long): Single<AccountInternal>
    fun saveAccount(account: AccountInternal)

    fun addBrushing(data: CreateBrushingData, profile: ProfileInternal)
    fun addBrushingSingle(data: CreateBrushingData, profile: ProfileInternal): Single<Brushing>
    @Deprecated(
        message = "Try to avoid blocking calls",
        replaceWith = ReplaceWith("addBrushingSingle")
    )
    fun addBrushingSync(data: CreateBrushingData, profile: ProfileInternal): Brushing
    fun assignBrushings(brushings: List<Brushing>, profile: ProfileInternal): Boolean
    fun getBrushingList(id: Long): Single<List<Brushing?>?>

    fun sendRefreshBroadcast()

    fun updateGoPirateData(data: UpdateGoPirateData?, profile: ProfileInternal?)
    fun getPractitioners(
        listener: KolibreeConnectorListener<Array<Practitioner>>,
        profile: ProfileInternal
    )

    fun revokePractitioner(
        token: String,
        listener: KolibreeConnectorListener<Boolean>
    )

    fun synchronizeBrushing(profileId: Long): Single<Boolean>
    fun synchronizeGoPirate(profileId: Long)

    /**
     * Non blocking operation that will update the profile picture in the backend
     */
    @Deprecated(message = "Use changeProfilePictureSingle")
    fun changeProfilePicture(profile: ProfileInternal, picturePath: String?)

    fun changeProfilePictureSingle(
        profile: ProfileInternal,
        picturePath: String?
    ): Single<ProfileInternal>

    /**
     * Deletes the profile remotely and, on success, locally.
     *
     * If the operation succeeds and the removed profile is the currently active profile, it sets
     * the owner profile as active
     *
     * @return Single that will emit true on success, false otherwise. If there's no account, it'll
     * emit [NoAccountException]
     */
    fun internalDeleteProfile(profileId: Long): Single<Boolean>
}
