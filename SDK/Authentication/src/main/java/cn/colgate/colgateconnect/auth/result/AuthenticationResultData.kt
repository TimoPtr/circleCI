package cn.colgate.colgateconnect.auth.result

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.models.IProfile

/**
 * This class contains profiles returned during login or account creation flow.
 */

@Keep
data class AuthenticationResultData(val profiles: List<IProfile>) : Parcelable {

    private constructor(parcel: Parcel) : this(ParcelableProfile.toProfileList(parcel))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(profiles.size)
        for (profile in profiles) {
            parcel.writeParcelable(toParcelableProfile(profile), flags)
        }
    }

    override fun describeContents() = 0

    private fun toParcelableProfile(profile: IProfile) = ParcelableProfile(
        profile.pictureUrl,
        profile.pictureLastModifier,
        profile.id,
        profile.firstName,
        profile.gender,
        profile.country,
        profile.handedness,
        profile.brushingGoalTime,
        profile.createdDate,
        profile.birthday)

    companion object CREATOR : Creator<AuthenticationResultData> {
        override fun createFromParcel(parcel: Parcel): AuthenticationResultData {
            return AuthenticationResultData(parcel)
        }

        override fun newArray(size: Int): Array<AuthenticationResultData?> {
            return arrayOfNulls(size)
        }
    }
}
