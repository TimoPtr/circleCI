package com.kolibree.android.sdk.core

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection

fun KolibreeService.createConnection(mac: String, name: String, model: ToothbrushModel): KLTBConnection =
    KolibreeServiceHelper.createConnection(this, mac, name, model)
