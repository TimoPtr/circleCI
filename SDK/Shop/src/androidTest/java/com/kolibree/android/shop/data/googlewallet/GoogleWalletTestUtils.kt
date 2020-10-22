/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import android.os.Handler
import android.os.Looper
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import io.reactivex.observers.TestObserver

internal inline fun <reified T : Any> taskWithException(
    e: Exception,
    onTaskReadyBlock: (task: Task<T>) -> TestObserver<T>,
    crossinline assertBlock: (TestObserver<T>) -> Unit
) {
    val taskSource = TaskCompletionSource<T>()

    val observer = onTaskReadyBlock.invoke(taskSource.task)

    taskSource.setException(e)

    Handler(Looper.getMainLooper()).postDelayed({ assertBlock.invoke(observer) }, 1)
}

internal inline fun <reified T : Any, reified R : Any> taskWithSuccess(
    result: T,
    onTaskReadyBlock: (task: Task<T>) -> TestObserver<R>,
    crossinline assertBlock: (TestObserver<R>) -> Unit,
    delay: Long = 1
) {
    val taskSource = TaskCompletionSource<T>()

    val observer = onTaskReadyBlock.invoke(taskSource.task)

    taskSource.setResult(result)

    Handler(Looper.getMainLooper()).postDelayed({ assertBlock.invoke(observer) }, delay)
}
