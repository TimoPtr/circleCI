/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.logging

import androidx.annotation.CallSuper
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.JavaLogger
import java.util.regex.Pattern
import timber.log.Timber

@VisibleForApp
abstract class KLTimberTree : Timber.DebugTree(), JavaLogger {

    override fun debug(message: String) {
        d(message)
    }

    override fun warning(message: String) {
        w(message)
    }

    override fun error(throwable: Throwable?, message: String) {
        e(throwable, message)
    }

    override fun error(message: String) {
        e(message)
    }

    /**
     * Transforms [message] so that the log line prints the class and line at which the log occurred
     *
     * If viewed from Android Studio's Logcat window, this will allow us to easily navigate to the log
     * invocation point
     *
     * Example
     * ```
     * KLNordicBleManager: writeAndNotifyOperation post command id 0x50 - (KLNordicBleManager.kt:221)
     * ```
     *
     * Known limitations:
     * - If [message] already contains parenthesis, navitation from Android Studio won't work
     *
     * In this case, you won't be able to navigate. But the information is still useful
     * ```
     * writeWithAck writeCharacteristic done(to: ...) - (KLNordicBleManager.kt:257)
     * ```
     *
     * - Overriden log functions will yield wrong navigation. For example
     * ```
    override fun log(priority: Int, message: String) {
    if (priority > Log.DEBUG)
    Timber.tag(TAG).log(priority, message)
    }
     * ```
     *
     * Printed line will always point to the Timber.tag invocation, not the origin of the log
     */
    @CallSuper
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        return getStackTrace()?.let { stackTraceElement ->
            val clazz = extractClassName(stackTraceElement)
            val lineNumber = stackTraceElement.lineNumber

            val newMessage = "$message - ($clazz:$lineNumber)"
            super.log(priority, tag, newMessage, t)
        } ?: super.log(priority, tag, message, t)
    }

    /**
     * Selects the appropriate stacktrace to extract the class name + line from
     *
     * A normal Timber.d invocation produces a stacktrace such as the following (index 4)
     *
     * ```
    stackTrace = {StackTraceElement[44]@18257}
    0 = {StackTraceElement@18262} "com.kolibree.android.logging.KLDebugTimberTree.log(KLTimberTree.kt:35)"
    1 = {StackTraceElement@18263} "timber.log.Timber$Tree.prepareLog(Timber.java:532)"
    2 = {StackTraceElement@18264} "timber.log.Timber$Tree.v(Timber.java:390)"
    3 = {StackTraceElement@18265} "timber.log.Timber$1.v(Timber.java:222)"
    4 = {StackTraceElement@18266} "com.kolibree.android.app.ui.fragment.BaseFragment.onPause(BaseFragment.java:92)"
    declaringClass = "com.kolibree.android.app.ui.fragment.BaseFragment"
    fileName = "BaseFragment.java"
    lineNumber = 92
    methodName = "onPause"
    shadow$_klass_ = {Class@6973} "class java.lang.StackTraceElement"
    shadow$_monitor_ = 0
    ```
     *
     * If it happens inside a lambda, the index is 5
     *
    ```
    stackTrace = {StackTraceElement[65]@18115}
    0 = {StackTraceElement@18117} "com.kolibree.android.logging.KLDebugTimberTree.log(KLTimberTree.kt:35)"
    1 = {StackTraceElement@18118} "timber.log.Timber$Tree.prepareLog(Timber.java:532)"
    2 = {StackTraceElement@18119} "timber.log.Timber$Tree.d(Timber.java:405)"
    3 = {StackTraceElement@18120} "timber.log.Timber$1.d(Timber.java:243)"
    4 = {StackTraceElement@18121} "timber.log.Timber.d(Timber.java:38)"
    declaringClass = "timber.log.Timber"
    fileName = "Timber.java"
    lineNumber = 38
    methodName = "d"
    shadow$_klass_ = {Class@6973} "class java.lang.StackTraceElement"
    shadow$_monitor_ = 0
    5 = {StackTraceElement@18122} "com.kolibree.android.sdk.core.KLTBConnectionDoctor$synchronizeBrushingMode$1
    .run(KLTBConnectionDoctor.kt:191)"
    ```
     *
     * By using the first StackTraceElement that doesn't contain Timber, I hope we'll always reference
     * the appropriate invocation point
     *
     * If we don't find any, we'll use Timber's standard log function
     */
    private fun getStackTrace(): StackTraceElement? {
        val stacktraces = Throwable().stackTrace
        check(stacktraces.size > CALL_STACK_INDEX) {
            "Synthetic stacktrace didn't have enough elements: are you using proguard?"
        }

        stacktraces.forEach { stackTraceElement ->
            if (!stackTraceElement.className.contains("Timber")) return stackTraceElement
        }

        return null
    }

    /**
     * Extract the class name without any anonymous class suffixes (e.g., `Foo$1`
     * becomes `Foo`).
     */
    private fun extractClassName(element: StackTraceElement): String? {
        var className = element.fileName

        if (className != null) {
            val matcher = anonymousClassPattern.matcher(className)
            if (matcher.find()) {
                className = matcher.replaceAll("")
            }
        }

        return className
    }

    @VisibleForApp
    companion object {

        @JvmStatic
        fun create(debug: Boolean): KLTimberTree {
            return if (debug) KLDebugTimberTree()
            else KLReleaseTimberTree()
        }
    }
}

private val anonymousClassPattern: Pattern = Pattern.compile("(\\$\\d+)+$")
private const val CALL_STACK_INDEX = 4
