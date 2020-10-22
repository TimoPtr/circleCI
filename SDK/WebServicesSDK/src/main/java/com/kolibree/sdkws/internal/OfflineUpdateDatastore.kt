package com.kolibree.sdkws.internal

internal interface OfflineUpdateDatastore {
    fun insertOrUpdate(newUpdate: com.kolibree.sdkws.internal.OfflineUpdateInternal): Boolean
    fun getOfflineUpdateForProfileId(profileId: Long, type: Int): com.kolibree.sdkws.internal.OfflineUpdateInternal?
    fun delete(profileId: Long, type: Int)
    fun truncate()
}
