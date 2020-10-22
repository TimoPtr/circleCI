/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbar

import android.graphics.drawable.AnimatedVectorDrawable
import androidx.appcompat.widget.AppCompatImageView
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.homeui.hum.R
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class ToolbarBindingAdapterTest : BaseUnitTest() {

    private val imageView: AppCompatImageView = mock()
    private val animatedVector: AnimatedVectorDrawable = mock()

    private val toothbrushConnectingIcon = ToolbarIcon.ToothbrushConnecting
    private val toothbrushConnectedIcon = ToolbarIcon.ToothbrushConnected

    override fun setup() {
        whenever(imageView.drawable).thenReturn(animatedVector)
    }

    @Test
    fun `toolBarIcon with start transition sets the image to the startTransition`() {
        imageView.setToolbarIcon(toothbrushConnectingIcon)

        verify(imageView).setImageResource(toothbrushConnectingIcon.startTransition)
        verify(imageView, never()).setImageResource(toothbrushConnectingIcon.mainIcon)
    }

    @Test
    fun `toolBarIcon without start transition sets the image to the mainIcon`() {
        imageView.setToolbarIcon(toothbrushConnectedIcon)

        verify(imageView).setImageResource(toothbrushConnectedIcon.mainIcon)
        verify(imageView, never()).setImageResource(toothbrushConnectedIcon.startTransition)
    }

    @Test
    fun `current icon with an end transition sets the image to the endTransition`() {
        whenever(imageView.getTag(eq(R.id.toothbrush_icon))).thenReturn(toothbrushConnectingIcon)

        imageView.setToolbarIcon(toothbrushConnectedIcon)

        verify(imageView).setImageResource(toothbrushConnectingIcon.endTransition)
        verify(imageView, never()).setImageResource(toothbrushConnectedIcon.startTransition)
        verify(imageView, never()).setImageResource(toothbrushConnectedIcon.mainIcon)
    }

    @Test
    fun `current animation is cancelled`() {
        imageView.setToolbarIcon(toothbrushConnectedIcon)

        verify(animatedVector).stop()
    }
}
