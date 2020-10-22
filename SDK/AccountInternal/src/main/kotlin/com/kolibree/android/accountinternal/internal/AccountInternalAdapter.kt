/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.internal

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.kolibree.android.annotation.VisibleForApp
import java.lang.reflect.Type

@VisibleForApp
class AccountInternalAdapter(private val gson: Gson) : JsonDeserializer<AccountInternal> {

    @Throws(JsonParseException::class)
    override fun deserialize(je: JsonElement, type: Type, jdc: JsonDeserializationContext): AccountInternal {

        val accountJson = je.asJsonObject
        val wechatJson: JsonObject? = je.asJsonObject.get(COLUMN_ACCOUNT_WC)?.asJsonObject

        val account = gson.fromJson(accountJson, AccountInternal::class.java)

        wechatJson?.let {
            val wechat = gson.fromJson(wechatJson, AccountWechat::class.java)
            account.setWechat(wechat)
        }
        return account
    }

    private companion object {
        private const val COLUMN_ACCOUNT_WC = "wechat"
    }
}
