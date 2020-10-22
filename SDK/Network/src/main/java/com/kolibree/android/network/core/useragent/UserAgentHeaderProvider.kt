package com.kolibree.android.network.core.useragent

import androidx.annotation.Keep

/** Created by lookash on 30/12/2018. */
@Keep
const val USER_AGENT = "User-Agent"

@Keep
abstract class UserAgentHeaderProvider {

    val userAgent: Pair<String, String>
        get() = USER_AGENT to userAgentValue

    protected abstract val userAgentValue: String
}
