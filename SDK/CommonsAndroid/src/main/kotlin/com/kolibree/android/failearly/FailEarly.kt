package com.kolibree.android.failearly

import android.os.Looper
import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import timber.log.Timber

/**
 * Created by lookashc on 29/01/19.
 *
 * Simple, yet powerful utility class for leveraging fail early approach
 * (otherwise known as offensive programming).
 *
 * It allows to early detect issues that may be lost in functional testing
 * (like non-propagated exceptions or detection of unexpected application states).
 *
 * Use it to indicate that app is in the state in which you doesn't expect it to be.
 *
 * NOTE:
 * Please be advised that debug app will stop immediately when one of fail() methods
 * is called. Beta and release apps will attempt recovery (if provided).
 */

typealias FailEarlyDelegate = (Throwable, (() -> Unit)) -> Unit

@Suppress("all")
@SuppressWarnings("all")
object FailEarly {

    internal lateinit var delegate: FailEarlyDelegate

    private var mainThreadExecutionTestEnabled: Boolean = true

    init {
        setupDefaultDelegate()
    }

    @JvmStatic
    @VisibleForApp
    fun overrideDelegateWith(delegate: FailEarlyDelegate) {
        this.delegate = delegate
    }

    @JvmStatic
    @VisibleForTesting
    fun testMainThreadExecution(mainThreadExecutionTestEnabled: Boolean) {
        this.mainThreadExecutionTestEnabled = mainThreadExecutionTestEnabled
    }

    @JvmStatic
    @Suppress
    fun failInConditionMet(condition: Boolean, message: String) =
        failInConditionMet(condition, message = message) { /* no-op */ }

    @JvmStatic
    @Suppress
    fun failInConditionMet(condition: Boolean, tag: String? = null, message: String) =
        failInConditionMet(condition, tag, message) { /* no-op */ }

    @JvmStatic
    @Suppress
    fun failInConditionMet(condition: Boolean, message: String, fallback: () -> Unit) {
        if (condition) {
            fail(message = message, fallback = fallback)
        }
    }

    @JvmStatic
    @Suppress
    fun failIfExecutedOnMainThread() = failInConditionMet(
        condition = mainThreadExecutionTestEnabled && Thread.currentThread() == Looper.getMainLooper().thread,
        message = "Code is executed on the main thread, please review your implementation"
    )

    @JvmStatic
    @Suppress
    fun failIfNotExecutedOnMainThread() = failInConditionMet(
        condition = mainThreadExecutionTestEnabled && Thread.currentThread() != Looper.getMainLooper().thread,
        message = "Code should be executed on the main thread, please review your implementation"
    )

    @JvmStatic
    @Suppress
    fun failInConditionMet(
        condition: Boolean,
        tag: String? = null,
        message: String,
        fallback: () -> Unit
    ) {
        if (condition) {
            fail(tag, message, fallback)
        }
    }

    @JvmStatic
    @Suppress
    fun fail(message: String) = fail(null, message) { /* no-op */ }

    @JvmStatic
    @Suppress
    fun fail(tag: String? = null, message: String) = fail(tag, message) { /* no-op */ }

    @JvmStatic
    @Suppress
    fun fail(tag: String? = null, exception: Throwable) = fail(tag, exception, null) { /* no-op */ }

    @JvmStatic
    @Suppress
    fun fail(tag: String? = null, exception: Throwable, message: String? = null) =
        fail(tag, exception, message) {
            /* no-op */
        }

    @Suppress
    @JvmStatic
    fun fail(
        tag: String? = null,
        exception: Throwable,
        message: String? = null,
        fallback: () -> Unit
    ) {
        val logException = FailEarlyException(exception)
        when {
            tag != null && message != null -> Timber.tag(tag).wtf(logException, message)
            tag != null -> Timber.tag(tag).wtf(logException)
            message != null -> Timber.wtf(logException, message)
            else -> Timber.wtf(logException)
        }
        delegate(logException, fallback)
    }

    @JvmStatic
    @Suppress
    fun fail(message: String, fallback: () -> Unit) =
        fail(tag = null, message = message, fallback = fallback)

    @JvmStatic
    @Suppress
    fun fail(tag: String?, message: String, fallback: () -> Unit) {
        val logException = FailEarlyException(message)
        when {
            tag != null -> Timber.tag(tag).wtf(logException)
            else -> Timber.wtf(logException)
        }
        delegate(logException, fallback)
    }

    private class FailEarlyException : Exception {

        constructor(message: String) : super(message)

        constructor(throwable: Throwable) : super(throwable)

        init {
            stackTrace = stackTrace.toMutableList().apply {
                removeAll { it.className == FailEarly::class.java.name }
            }.toTypedArray()
        }
    }
}
