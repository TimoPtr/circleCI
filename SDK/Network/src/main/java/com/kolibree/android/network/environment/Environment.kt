/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.network.environment

import androidx.annotation.Keep
import com.kolibree.android.annotation.VisibleForApp

/** Created by miguelaragues on 6/3/18.  */
@Keep
enum class Environment : Endpoint {
    CHINA {
        override fun url(): String {
            return "https://connect.colgate.com.cn"
        }
    },

    STAGING {
        override fun url(): String {
            return "https://api.s.kolibree.com"
        }
    },

    DEV {
        override fun url(): String {
            return "https://api.d.kolibree.com"
        }
    },

    PRODUCTION {
        override fun url(): String {
            return "https://api.p.kolibree.com"
        }
    },

    CUSTOM {
        override fun url(): String {
            throw IllegalAccessError("Do not read url from CUSTOM. Use EnvironmentManager.")
        }
    };

    @Keep
    companion object {
        @JvmStatic
        fun fromUrl(url: String): Environment =
            values().filter { it != CUSTOM }.firstOrNull { it.url() == url } ?: CUSTOM
    }
}

@VisibleForApp
data class CustomEnvironment(private val url: String) : Endpoint {
    override fun url() = url

    override fun toString() = url
}

@Keep
interface Endpoint {
    fun url(): String
}
