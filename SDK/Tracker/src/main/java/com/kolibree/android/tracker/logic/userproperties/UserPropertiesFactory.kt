/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.logic.userproperties

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.getCountry
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.tracker.logic.userproperties.UserProperties.ACCOUNT
import com.kolibree.android.tracker.logic.userproperties.UserProperties.BETA_USER
import com.kolibree.android.tracker.logic.userproperties.UserProperties.COUNTRY
import com.kolibree.android.tracker.logic.userproperties.UserProperties.FIRMWARE_VERSION
import com.kolibree.android.tracker.logic.userproperties.UserProperties.FIRST_DAY_OF_WEEK
import com.kolibree.android.tracker.logic.userproperties.UserProperties.FIRST_NAME
import com.kolibree.android.tracker.logic.userproperties.UserProperties.GENDER
import com.kolibree.android.tracker.logic.userproperties.UserProperties.GENDER_FEMALE
import com.kolibree.android.tracker.logic.userproperties.UserProperties.GENDER_MALE
import com.kolibree.android.tracker.logic.userproperties.UserProperties.GENDER_UNSET
import com.kolibree.android.tracker.logic.userproperties.UserProperties.HANDEDNESS
import com.kolibree.android.tracker.logic.userproperties.UserProperties.HANDEDNESS_LEFT
import com.kolibree.android.tracker.logic.userproperties.UserProperties.HANDEDNESS_RIGHT
import com.kolibree.android.tracker.logic.userproperties.UserProperties.HANDEDNESS_UNKNOWN
import com.kolibree.android.tracker.logic.userproperties.UserProperties.HARDWARE_VERSION
import com.kolibree.android.tracker.logic.userproperties.UserProperties.LOCALE
import com.kolibree.android.tracker.logic.userproperties.UserProperties.MAC_ADDRESS
import com.kolibree.android.tracker.logic.userproperties.UserProperties.PROFILE
import com.kolibree.android.tracker.logic.userproperties.UserProperties.PUB_ID
import com.kolibree.android.tracker.logic.userproperties.UserProperties.REGULAR_USER
import com.kolibree.android.tracker.logic.userproperties.UserProperties.SERIAL_NUMBER
import com.kolibree.android.tracker.logic.userproperties.UserProperties.STUDY_NAME
import com.kolibree.android.tracker.logic.userproperties.UserProperties.TOOTHBRUSH_MODEL
import com.kolibree.android.tracker.logic.userproperties.UserProperties.USER_TYPE
import com.kolibree.android.tracker.studies.StudiesForProfileUseCase
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.InternalKolibreeConnector
import io.reactivex.schedulers.Schedulers
import java.util.HashMap
import java.util.Locale
import javax.inject.Inject
import org.threeten.bp.temporal.WeekFields

internal class UserPropertiesFactory @Inject constructor(
    private val studiesForProfileUseCase: StudiesForProfileUseCase
) {

    private var accountId: Long? = null
    private var profileId: Long? = null
    private var pubId: String? = null
    private var firstName: String? = null
    private var gender = GENDER_UNSET
    private var country: String? = null
    private var handedness = HANDEDNESS_UNKNOWN
    private var userType: String? = null
    private var studyName: String? = null
    private var locale: String? = null
    private var firstDayOfWeek: String? = null

    private var hardwareVersion: String? = null
    private var firmwareVersion: String? = null
    private var macAddress: String? = null
    private var serialNumber: String? = null
    private var toothbrushModel: String? = null

    fun fill(connector: IKolibreeConnector, latestUsedConnection: KLTBConnection?) {
        accountId = getAccountId(connector)
        profileId = getProfileId(connector)
        pubId = getPubId(connector)
        firstName = getFirstName(connector)
        gender = getGender(connector)
        country = getUserCountry()
        handedness = getHandedness(connector)
        userType = getUserType(connector)
        studyName = getStudies(connector)
        locale = getLocale()
        firstDayOfWeek = getFirstDayOfWeek()

        populateToothbrushInfo(latestUsedConnection)
    }

    fun toMap(): Map<String, String?> {
        val params = HashMap<String, String?>()
        params[ACCOUNT] = accountId?.toString()
        params[PROFILE] = profileId?.toString()
        params[PUB_ID] = pubId
        params[HARDWARE_VERSION] = hardwareVersion
        params[FIRMWARE_VERSION] = firmwareVersion
        params[MAC_ADDRESS] = macAddress
        params[SERIAL_NUMBER] = serialNumber
        params[TOOTHBRUSH_MODEL] = toothbrushModel
        params[FIRST_NAME] = firstName
        params[GENDER] = gender
        params[COUNTRY] = country
        params[HANDEDNESS] = handedness
        params[USER_TYPE] = userType
        params[STUDY_NAME] = studyName
        params[LOCALE] = locale
        params[FIRST_DAY_OF_WEEK] = firstDayOfWeek

        return params
    }

    private fun getAccountId(connector: IKolibreeConnector): Long? =
        connector.accountId.let { if (it == -1L) null else it }

    private fun getProfileId(connector: IKolibreeConnector): Long? =
        connector.currentProfile?.id

    private fun getPubId(connector: IKolibreeConnector): String? =
        connector.pubId

    private fun getFirstName(connector: IKolibreeConnector): String? =
        connector.currentProfile?.firstName

    private fun getGender(connector: IKolibreeConnector): String =
        connector.currentProfile?.let {
            if (it.isMale()) GENDER_MALE else GENDER_FEMALE
        } ?: GENDER_UNSET

    private fun getHandedness(connector: IKolibreeConnector): String =
        connector.currentProfile?.let {
            if (it.isRightHanded()) HANDEDNESS_RIGHT else HANDEDNESS_LEFT
        } ?: HANDEDNESS_UNKNOWN

    private fun getUserCountry() = getCountry()

    private fun getUserType(connector: IKolibreeConnector): String? =
        (connector as? InternalKolibreeConnector)?.currentAccount()?.let {
            if (it.isBeta) BETA_USER else REGULAR_USER
        }

    private fun getStudies(connector: IKolibreeConnector) =
        getProfileId(connector)
            ?.let {
                studiesForProfileUseCase
                    .provide(it)
                    .subscribeOn(Schedulers.newThread())
                    // Ugly but making the tracker reactive would have involved too many changes
                    .blockingGet()
            }

    private fun getLocale() = Locale.getDefault().displayName

    private fun getFirstDayOfWeek() = WeekFields.of(Locale.getDefault()).firstDayOfWeek.name

    private fun populateToothbrushInfo(latestUsedConnection: KLTBConnection?) {
        toothbrushModel = null
        serialNumber = null
        hardwareVersion = null
        firmwareVersion = null
        macAddress = null

        if (latestUsedConnection != null) {
            val toothbrush = latestUsedConnection.toothbrush()
            macAddress = toothbrush.mac
            firmwareVersion = toothbrush.firmwareVersion.toAnalyticsString()
            hardwareVersion = toothbrush.hardwareVersion.toAnalyticsString()
            serialNumber = toothbrush.serialNumber
            toothbrushModel = getToothbrushModel(toothbrush.model)
        }
    }

    companion object {

        @VisibleForTesting
        fun getToothbrushModel(toothbrushModel: ToothbrushModel): String? {
            return when (toothbrushModel) {
                ToothbrushModel.ARA -> "Ara"
                ToothbrushModel.CONNECT_M1 -> "M1"
                ToothbrushModel.CONNECT_E1 -> "E1"
                ToothbrushModel.CONNECT_E2 -> "E2"
                ToothbrushModel.CONNECT_B1 -> "B1"
                ToothbrushModel.PLAQLESS -> "PQL"
                else -> null
            }
        }
    }
}

private fun HardwareVersion.toAnalyticsString(): String? =
    let { if (it == HardwareVersion.NULL) null else it }?.toString()

private fun SoftwareVersion.toAnalyticsString(): String? =
    let { if (it == SoftwareVersion.NULL) null else it }?.toString()
