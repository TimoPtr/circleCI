package com.kolibree.android.sdk.core

/**
 * Created by aurelien on 10/08/17.
 *
 *
 * Adds a method to clear cached values when toothbrush connection is terminated
 */
internal interface DataCache {

    /** Clear cached values  */
    fun clearCache()
}
