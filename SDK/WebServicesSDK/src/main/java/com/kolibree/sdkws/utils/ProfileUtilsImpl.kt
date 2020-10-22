package com.kolibree.sdkws.utils

import com.kolibree.sdkws.core.IKolibreeConnector
import javax.inject.Inject

internal class ProfileUtilsImpl @Inject constructor(private val connector: IKolibreeConnector) :
    ProfileUtils {

    override val isAllowedToBrush: Boolean
        get() = connector.withCurrentProfile()?.isAllowedToBrush ?: false
}
