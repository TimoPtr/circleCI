package com.kolibree.android.sba.testbrushing.results.hint

import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sba.testbrushing.results.hint.ResultHintsPreferencesImpl.Companion.CHANGE_VIEW_HINT_KEY
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class ResultHintsPreferencesImplTest : BaseUnitTest() {

    private val preferences = mock<SharedPreferences>()

    internal lateinit var hintsPreferences: ResultHintsPreferencesImpl

    override fun setup() {
        super.setup()

        hintsPreferences = spy(ResultHintsPreferencesImpl(preferences))
    }

    @Test
    fun `isChangeViewHintVisible invokes preferences with key CHANGE_VIEW_HINT_KEY`() {
        doReturn(true).whenever(preferences).getBoolean(CHANGE_VIEW_HINT_KEY, true)

        hintsPreferences.isChangeViewHintVisible()

        verify(preferences).getBoolean(CHANGE_VIEW_HINT_KEY, true)
    }

    @Test
    fun `removeChangeViewHint sets preferences key CHANGE_VIEW_HINT_KEY to false`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)
        whenever(editor.putBoolean(any(), any())).thenReturn(editor)

        hintsPreferences.removeChangeViewHint()

        verify(preferences).edit()
        verify(editor).putBoolean(CHANGE_VIEW_HINT_KEY, false)
    }
}
