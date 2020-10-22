/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.profile

import com.kolibree.account.utils.ToothbrushForgetter
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.CompletableSubject
import org.junit.Assert.assertTrue
import org.junit.Test

class ForgetDeletedProfileToothbrushesHookTest {
    private val toothbrushForgetter: ToothbrushForgetter = mock()
    private val hook = ForgetDeletedProfileToothbrushesHook(toothbrushForgetter)

    @Test
    fun `onProfileDeleted returns toothbrushForgetter forgetOwnedByProfile`() {
        val deletedProfileId = 54L
        val forgetSubject = CompletableSubject.create()
        whenever(toothbrushForgetter.forgetOwnedByProfile(deletedProfileId))
            .thenReturn(forgetSubject)

        val observer = hook.onProfileDeleted(deletedProfileId).test().assertNotComplete()

        assertTrue(forgetSubject.hasObservers())
        forgetSubject.onComplete()

        observer.assertComplete()
    }
}
