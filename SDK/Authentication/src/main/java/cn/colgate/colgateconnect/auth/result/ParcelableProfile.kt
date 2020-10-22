package cn.colgate.colgateconnect.auth.result

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.commons.profile.SourceApplication
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

@Parcelize
internal data class ParcelableProfile(
    override val pictureUrl: String? = null,
    override val pictureLastModifier: String? = null,
    override val id: Long,
    override val firstName: String,
    override val gender: Gender,
    override val country: String?,
    override val handedness: Handedness,
    override val brushingGoalTime: Int,
    override val createdDate: String,
    override val birthday: LocalDate?,
    override val sourceApplication: SourceApplication? = null
) : IProfile, Parcelable {

    @SuppressLint("SdkPublicClassInNonKolibreePackage")
    companion object {
        fun toProfileList(parcel: Parcel): List<IProfile> {
            val profiles = mutableListOf<IProfile>()
            val size = parcel.readInt()
            for (i in 0 until size) {
                parcel.readParcelable<ParcelableProfile>(ParcelableProfile::class.java.classLoader)?.let {
                    profiles.add(it)
                }
            }
            return profiles
        }
    }
}
