/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.animation

import com.kolibree.kml.MouthZone16.LoIncExt
import com.kolibree.kml.MouthZone16.LoIncInt
import com.kolibree.kml.MouthZone16.LoMolLeExt
import com.kolibree.kml.MouthZone16.LoMolLeInt
import com.kolibree.kml.MouthZone16.LoMolLeOcc
import com.kolibree.kml.MouthZone16.LoMolRiExt
import com.kolibree.kml.MouthZone16.LoMolRiInt
import com.kolibree.kml.MouthZone16.LoMolRiOcc
import com.kolibree.kml.MouthZone16.UpIncExt
import com.kolibree.kml.MouthZone16.UpIncInt
import com.kolibree.kml.MouthZone16.UpMolLeExt
import com.kolibree.kml.MouthZone16.UpMolLeInt
import com.kolibree.kml.MouthZone16.UpMolLeOcc
import com.kolibree.kml.MouthZone16.UpMolRiExt
import com.kolibree.kml.MouthZone16.UpMolRiInt
import com.kolibree.kml.MouthZone16.UpMolRiOcc
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert.assertEquals
import org.junit.Test

/** [BaseBrushHeadAnimationMapper] tests */
class BaseBrushHeadAnimationMapperTest {

    private val mapper = spy(BaseBrushHeadAnimationMapperStub())

    @Test
    fun getAnimationForZone_returnsSIMPLE_ANIMATIONForLoMolLeOcc() {
        assertEquals(BaseBrushHeadAnimationMapper.simpleAnimation, mapper.getAnimationForZone(LoMolLeOcc))
    }

    @Test
    fun getAnimationForZone_returnsSIMPLE_ANIMATIONForLoMolRiOcc() {
        assertEquals(BaseBrushHeadAnimationMapper.simpleAnimation, mapper.getAnimationForZone(LoMolRiOcc))
    }

    @Test
    fun getAnimationForZone_returnsSIMPLE_ANIMATIONForUpMolLeOcc() {
        assertEquals(BaseBrushHeadAnimationMapper.simpleAnimation, mapper.getAnimationForZone(UpMolLeOcc))
    }

    @Test
    fun getAnimationForZone_returnsSIMPLE_ANIMATIONForUpMolRiOcc() {
        assertEquals(BaseBrushHeadAnimationMapper.simpleAnimation, mapper.getAnimationForZone(UpMolRiOcc))
    }

    @Test
    fun getAnimationForZone_returnsPLAQLESS_ANIMATIONForLoMolRiExt() {
        assertEquals(BaseBrushHeadAnimationMapper.brushHeadAnimation, mapper.getAnimationForZone(LoMolRiExt))
    }

    @Test
    fun getAnimationForZone_returnsPLAQLESS_ANIMATIONForLoMolLeExt() {
        assertEquals(
            BaseBrushHeadAnimationMapper.brushHeadAnimation,
            mapper.getAnimationForZone(LoMolLeExt)
        )
    }

    @Test
    fun getAnimationForZone_returnsPLAQLESSANIMATIONForUpMolLeExt() {
        assertEquals(BaseBrushHeadAnimationMapper.brushHeadAnimation, mapper.getAnimationForZone(UpMolLeExt))
    }

    @Test
    fun getAnimationForZone_returnsPLAQLESS_ANIMATIONForUpMolRiExt() {
        assertEquals(
            BaseBrushHeadAnimationMapper.brushHeadAnimation,
            mapper.getAnimationForZone(UpMolRiExt)
        )
    }

    @Test
    fun getAnimationForZone_returnsPLAQLESS_ANIMATIONForUpIncExt() {
        assertEquals(BaseBrushHeadAnimationMapper.brushHeadAnimation, mapper.getAnimationForZone(UpIncExt))
    }

    @Test
    fun getAnimationForZone_returnsPLAQLESS_ANIMATIONForLoIncExt() {
        assertEquals(
            BaseBrushHeadAnimationMapper.brushHeadAnimation,
            mapper.getAnimationForZone(LoIncExt)
        )
    }

    @Test
    fun getAnimationForZone_returnsPLAQLESS_ANIMATIONForLoMolRiInt() {
        assertEquals(
            BaseBrushHeadAnimationMapper.brushHeadAnimation,
            mapper.getAnimationForZone(LoMolRiInt)
        )
    }

    @Test
    fun getAnimationForZone_returnsPLAQLESS_ANIMATIONForLoMolLeInt() {
        assertEquals(BaseBrushHeadAnimationMapper.brushHeadAnimation, mapper.getAnimationForZone(LoMolLeInt))
    }

    @Test
    fun getAnimationForZone_returnsPLAQLESS_ANIMATIONForUpMolLeInt() {
        assertEquals(
            BaseBrushHeadAnimationMapper.brushHeadAnimation,
            mapper.getAnimationForZone(UpMolLeInt)
        )
    }

    @Test
    fun getAnimationForZone_returnsPLAQLESS_ANIMATIONForUpMolRiInt() {
        assertEquals(
            BaseBrushHeadAnimationMapper.brushHeadAnimation,
            mapper.getAnimationForZone(UpMolRiInt)
        )
    }

    @Test
    fun `getAnimationForZone invokes implementations of upIncIntAnimation for UpIncInt`() {
        mapper.getAnimationForZone(UpIncInt)
        verify(mapper).upIncIntAnimation
    }

    @Test
    fun `getAnimationForZone invokes implementations of loIncIntAnimation for LoIncInt`() {
        mapper.getAnimationForZone(LoIncInt)
        verify(mapper).loIncIntAnimation
    }
}

private class BaseBrushHeadAnimationMapperStub : BaseBrushHeadAnimationMapper() {

    override val upIncIntAnimation = mock<Animation>()
    override val loIncIntAnimation = mock<Animation>()
}
