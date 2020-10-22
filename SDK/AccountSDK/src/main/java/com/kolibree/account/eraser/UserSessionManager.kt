package com.kolibree.account.eraser

import android.annotation.SuppressLint

/**
 * Exposes methods to manage a logged in user session
 *
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
interface UserSessionManager {

    /**
     * Clears all data associated to a logged in account
     *
     * - Remove stored content
     * - Remove stored preferences, except those starting with `secret_` or those from
     * `package cn.colgate.colgateconnect`
     * - Cancel scheduled tasks
     *
     * Invoking this method when there's no user logged in can yield unexpected results
     */
    fun reset()
}
