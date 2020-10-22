/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pulsingdot.data

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.pulsingdot.data.PulsingDotPersistence.BRUSH_BETTER
import com.kolibree.android.app.ui.home.pulsingdot.data.PulsingDotPersistence.FREQUENCY_CHART
import com.kolibree.android.app.ui.home.pulsingdot.data.PulsingDotPersistence.LAST_BRUSHING_SESSION
import com.kolibree.android.app.ui.home.pulsingdot.data.PulsingDotPersistence.SMILE
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

class PulsingDotProviderImplTest : BaseUnitTest() {

    private val preferences: SharedPreferences = mock()

    private lateinit var pulsingDotProviderImpl: PulsingDotProviderImpl

    @Before
    fun setUp() {
        val context: Context = mock()
        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(preferences)

        pulsingDotProviderImpl = PulsingDotProviderImpl(context)
    }

    @Test
    fun `getNumberTimeShown query the preferences for dot smile`() {
        pulsingDotProviderImpl.getTimesShown(SMILE)
        verify(preferences).getInt(KEY_DOT_SMILE_TIMES_SHOWN, 0)

        verifyNoMoreInteractions(preferences)
    }

    @Test
    fun `getNumberTimeShown query the preferences for dot last brushing`() {
        pulsingDotProviderImpl.getTimesShown(LAST_BRUSHING_SESSION)
        verify(preferences).getInt(KEY_DOT_LAST_BRUSHING_TIMES_SHOWN, 0)

        verifyNoMoreInteractions(preferences)
    }

    @Test
    fun `getNumberTimeShown query the preferences for dot brush better`() {
        pulsingDotProviderImpl.getTimesShown(BRUSH_BETTER)
        verify(preferences).getInt(KEY_DOT_BRUSH_BETTER_TIMES_SHOWN, 0)

        verifyNoMoreInteractions(preferences)
    }

    @Test
    fun `getNumberTimeShown query the preferences for dot frequency time`() {
        pulsingDotProviderImpl.getTimesShown(FREQUENCY_CHART)
        verify(preferences).getInt(KEY_DOT_FREQUENCY_TIMES_SHOWN, 0)

        verifyNoMoreInteractions(preferences)
    }

    @Test
    fun `incNumberTimeShown save the incremented value for dot smiles`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)

        whenever(preferences.getInt(KEY_DOT_SMILE_TIMES_SHOWN, 0)).thenReturn(4)
        pulsingDotProviderImpl.incTimesShown(SMILE)
        verify(editor).putInt(KEY_DOT_SMILE_TIMES_SHOWN, 5)
    }

    @Test
    fun `incNumberTimeShown save the incremented value for dot last brushing`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)

        whenever(preferences.getInt(KEY_DOT_LAST_BRUSHING_TIMES_SHOWN, 0)).thenReturn(7)
        pulsingDotProviderImpl.incTimesShown(LAST_BRUSHING_SESSION)
        verify(editor).putInt(KEY_DOT_LAST_BRUSHING_TIMES_SHOWN, 8)
    }

    @Test
    fun `incNumberTimeShown save the incremented value for dot brush better`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)

        whenever(preferences.getInt(KEY_DOT_BRUSH_BETTER_TIMES_SHOWN, 0)).thenReturn(2)
        pulsingDotProviderImpl.incTimesShown(BRUSH_BETTER)
        verify(editor).putInt(KEY_DOT_BRUSH_BETTER_TIMES_SHOWN, 3)
    }

    @Test
    fun `incNumberTimeShown save the incremented value for dot frequency chart`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)

        whenever(preferences.getInt(KEY_DOT_FREQUENCY_TIMES_SHOWN, 0)).thenReturn(0)
        pulsingDotProviderImpl.incTimesShown(FREQUENCY_CHART)
        verify(editor).putInt(KEY_DOT_FREQUENCY_TIMES_SHOWN, 1)
    }

    @Test
    fun `isClicked query the preferences for dot smile`() {
        pulsingDotProviderImpl.isClicked(SMILE)
        verify(preferences).getBoolean(KEY_DOT_SMILE_IS_CLICKED, false)

        verifyNoMoreInteractions(preferences)
    }

    @Test
    fun `isClicked query the preferences for dot last brushing`() {
        pulsingDotProviderImpl.isClicked(LAST_BRUSHING_SESSION)
        verify(preferences).getBoolean(KEY_DOT_LAST_BRUSHING_IS_CLICKED, false)

        verifyNoMoreInteractions(preferences)
    }

    @Test
    fun `isClicked query the preferences for dot brush better`() {
        pulsingDotProviderImpl.isClicked(BRUSH_BETTER)
        verify(preferences).getBoolean(KEY_DOT_BRUSH_BETTER_IS_CLICKED, false)

        verifyNoMoreInteractions(preferences)
    }

    @Test
    fun `isClicked query the preferences for dot frequency time`() {
        pulsingDotProviderImpl.isClicked(FREQUENCY_CHART)
        verify(preferences).getBoolean(KEY_DOT_FREQUENCY_IS_CLICKED, false)

        verifyNoMoreInteractions(preferences)
    }

    @Test
    fun `setIsClicked save the value for dot smiles`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)

        pulsingDotProviderImpl.setIsClicked(SMILE)
        verify(editor).putBoolean(KEY_DOT_SMILE_IS_CLICKED, true)
    }

    @Test
    fun `setIsClicked save the value for dot last brushing`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)

        pulsingDotProviderImpl.setIsClicked(LAST_BRUSHING_SESSION)
        verify(editor).putBoolean(KEY_DOT_LAST_BRUSHING_IS_CLICKED, true)
    }

    @Test
    fun `setIsClicked save the value for dot brush better`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)

        pulsingDotProviderImpl.setIsClicked(BRUSH_BETTER)
        verify(editor).putBoolean(KEY_DOT_BRUSH_BETTER_IS_CLICKED, true)
    }

    @Test
    fun `setIsClicked save the value for dot frequency chart`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)

        pulsingDotProviderImpl.setIsClicked(FREQUENCY_CHART)
        verify(editor).putBoolean(KEY_DOT_FREQUENCY_IS_CLICKED, true)
    }

    @Test
    fun `isExplanationShown query the preferences for explanation state`() {
        pulsingDotProviderImpl.isExplanationShown()
        verify(preferences).getBoolean(HAS_SHOWN_EXPLANATION, false)

        verifyNoMoreInteractions(preferences)
    }

    @Test
    fun `setExplanationShown save the value for the explanation`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)

        pulsingDotProviderImpl.setExplanationShown()
        verify(editor).putBoolean(HAS_SHOWN_EXPLANATION, true)
    }

    companion object {
        private const val KEY_DOT_SMILE_TIMES_SHOWN = "dot_smile_times_shown"
        private const val KEY_DOT_LAST_BRUSHING_TIMES_SHOWN =
            "dot_last_brushing_session_times_shown"
        private const val KEY_DOT_BRUSH_BETTER_TIMES_SHOWN = "dot_brush_better_times_shown"
        private const val KEY_DOT_FREQUENCY_TIMES_SHOWN = "dot_frequency_chart_times_shown"

        private const val KEY_DOT_SMILE_IS_CLICKED = "dot_smile_is_clicked"
        private const val KEY_DOT_LAST_BRUSHING_IS_CLICKED = "dot_last_brushing_session_is_clicked"
        private const val KEY_DOT_BRUSH_BETTER_IS_CLICKED = "dot_brush_better_is_clicked"
        private const val KEY_DOT_FREQUENCY_IS_CLICKED = "dot_frequency_chart_is_clicked"

        private const val HAS_SHOWN_EXPLANATION = "has_shown_explanation"
    }
}
