package com.kolibree.android.toothbrushupdate

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.toothbrushupdate.GruwareKey.Companion.create
import com.kolibree.sdkws.data.model.GruwareData
import java.util.HashMap
import javax.inject.Inject

@AppScope
internal class GruwareDataStore @Inject constructor() {
    private val gruwareMap: MutableMap<GruwareKey, GruwareData?> = HashMap()

    fun containsGruwareFor(connection: KLTBConnection): Boolean {
        val gruwareKey = getGruwareKey(connection)
        return gruwareMap.containsKey(gruwareKey)
    }

    fun gruwareFor(connection: KLTBConnection): GruwareData? {
        val gruwareKey = getGruwareKey(connection)
        return gruwareMap[gruwareKey]
    }

    private fun getGruwareKey(connection: KLTBConnection): GruwareKey {
        return create(connection)
    }

    fun saveGruware(connection: KLTBConnection, gruwareData: GruwareData) {
        gruwareMap[getGruwareKey(connection)] = gruwareData
    }
}
