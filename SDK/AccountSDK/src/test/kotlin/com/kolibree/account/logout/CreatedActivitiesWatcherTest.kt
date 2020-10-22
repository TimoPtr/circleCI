/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.logout

import android.app.Activity
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import java.lang.ref.WeakReference
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class CreatedActivitiesWatcherTest : BaseUnitTest() {

    /*
    lifeCycleWatcher
     */
    @Test
    fun `onActivityPaused does not touch startedActivities`() {
        CreatedActivitiesWatcher().verifyWeakReferenceNotRemovedNorAdded {
            onActivityPaused(mock())
        }
    }

    @Test
    fun `onActivityResumed does not touch startedActivities`() {
        CreatedActivitiesWatcher().verifyWeakReferenceNotRemovedNorAdded {
            onActivityResumed(mock())
        }
    }

    @Test
    fun `onActivityStarted does not touch startedActivities`() {
        CreatedActivitiesWatcher().verifyWeakReferenceNotRemovedNorAdded {
            onActivityStarted(mock())
        }
    }

    @Test
    fun `onActivitySaveInstanceState does not touch startedActivities`() {
        CreatedActivitiesWatcher().verifyWeakReferenceNotRemovedNorAdded {
            onActivitySaveInstanceState(mock(), mock())
        }
    }

    @Test
    fun `onActivityStopped does not touch startedActivities`() {
        CreatedActivitiesWatcher().verifyWeakReferenceNotRemovedNorAdded {
            onActivityStopped(mock())
        }
    }

    @Test
    fun `onActivityDestroyed does not touch startedActivities`() {
        CreatedActivitiesWatcher().verifyWeakReferenceNotRemovedNorAdded {
            onActivityDestroyed(mock())
        }
    }

    @Test
    fun `onActivityCreated adds Activity to startedActivities`() {
        CreatedActivitiesWatcher().apply {
            val expectedActivity = mock<Activity>()

            assertTrue(startedActivities.isEmpty())

            onActivityCreated(expectedActivity, mock())

            assertEquals(1, startedActivities.size)
            assertEquals(expectedActivity, startedActivities.single().get())
        }
    }

    /*
    finishActivitiesReverseOrder
     */

    @Test
    fun `finishActivitiesReverseOrder invokes finish on every activity starting from the last one added`() {
        CreatedActivitiesWatcher().apply {
            val firstActivity = mock<Activity>()
            val secondActivity = mock<Activity>()

            startedActivities.add(WeakReference(firstActivity))
            startedActivities.add(WeakReference(secondActivity))

            finishActivitiesReverseOrder()

            inOrder(firstActivity, secondActivity) {
                verify(secondActivity).finish()
                verify(firstActivity).finish()
            }
        }
    }

    @Test
    fun `finishActivitiesReverseOrder does not crash if an activity was garbage collected`() {
        CreatedActivitiesWatcher().apply {
            val firstActivity = mock<Activity>()
            val secondActivity = mock<Activity>()

            val firstWeakReference = WeakReference(firstActivity)
            startedActivities.add(firstWeakReference)
            startedActivities.add(WeakReference(secondActivity))

            firstWeakReference.clear()
            assertNull(firstWeakReference.get())

            finishActivitiesReverseOrder()

            verify(secondActivity).finish()
        }
    }

    /*
    clear
     */

    @Test
    fun `clear removes all activities in startedActivities`() {
        CreatedActivitiesWatcher().apply {
            val initialActivity = mock<Activity>()

            startedActivities.add(WeakReference(initialActivity))

            clear()

            assertTrue(startedActivities.isEmpty())
        }
    }

    /*
    UTILS
     */

    private inline fun CreatedActivitiesWatcher.verifyWeakReferenceNotRemovedNorAdded(block: CreatedActivitiesWatcher.() -> Unit) {
        val expectedWeakActivity = WeakReference<Activity>(mock())

        startedActivities.add(expectedWeakActivity)

        block.invoke(this)

        assertEquals(expectedWeakActivity, startedActivities.single())
    }
}
