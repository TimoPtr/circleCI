/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.quiz

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushingquiz.logic.models.QuizScreen
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class QuizPagerAdapterTest : BaseUnitTest() {
    private val adapter = spy(QuizPagerAdapter(mock()))

    @Test
    fun `update does not invoke notifyDataSetChanged if screen number hasn't changed`() {
        adapter.update(listOf())

        verify(adapter, never()).notifyDataSetChanged()
    }

    @Test
    fun `update invokes notifyDataSetChanged if screen number has changed`() {
        doNothing().whenever(adapter).notifyDataSetChanged()

        adapter.update(listOf(QuizScreen(0, mapOf())))

        verify(adapter).notifyDataSetChanged()
    }
}
