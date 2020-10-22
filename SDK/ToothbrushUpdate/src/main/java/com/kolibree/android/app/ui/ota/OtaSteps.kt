/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType
import com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER
import com.kolibree.android.failearly.FailEarly

/**
 * Holds the collection of non-empty steps resulting from the combination of [KLTBConnection] and [GruwareData]
 *
 * [steps] will be empty if we should not update anything
 */
internal data class OtaSteps @VisibleForTesting constructor(
    private val steps: List<AvailableUpdateStep>
) : List<AvailableUpdateStep> by steps {
    companion object {
        val EMPTY = OtaSteps(listOf())

        fun create(availableUpdates: List<AvailableUpdate>): OtaSteps {
            if (availableUpdates.isEmpty()) return EMPTY

            preventTooManyUpdates(availableUpdates)

            preventDuplicatedUpdates(availableUpdates)

            preventOnlyBootloader(availableUpdates)

            return OtaSteps(mapToSteps(availableUpdates))
        }

        private fun preventOnlyBootloader(availableUpdates: List<AvailableUpdate>) {
            if (availableUpdates.size == 1 && availableUpdates.single().type == TYPE_BOOTLOADER) {
                FailEarly.fail(
                    "Bootloader update must always be followed by a Firmware update ($availableUpdates)"
                )
                throw IllegalArgumentException(
                    "Bootloader update must always be followed by a Firmware update ($availableUpdates)"
                )
            }
        }

        private fun preventDuplicatedUpdates(availableUpdates: List<AvailableUpdate>) {
            if (availableUpdates.distinct().size != availableUpdates.size) {
                FailEarly.fail("Can't have duplicated UpdateType in $availableUpdates")
                throw IllegalArgumentException("Can't have duplicated UpdateType in $availableUpdates")
            }
        }

        private fun preventTooManyUpdates(availableUpdates: List<AvailableUpdate>) {
            if (availableUpdates.size > UpdateType.values().size) {
                FailEarly.fail(
                    "Can't have more than ${UpdateType.values().size} AvailableUpdates ($availableUpdates)"
                )
                throw IllegalArgumentException(
                    "Can't have more than ${UpdateType.values().size} AvailableUpdates ($availableUpdates)"
                )
            }
        }

        private fun mapToSteps(availableUpdates: List<AvailableUpdate>): List<AvailableUpdateStep> {
            val sortedUpdates =
                availableUpdates
                    .filterNot { it.isEmpty() }
                    .sortedWith(compareBy { it.type.order })

            val totalSteps = sortedUpdates.size

            if (totalSteps == 0) return listOf()

            val maxProgressPerStep = FULL_PROGRESS / totalSteps

            var initialProgress = 0
            return sortedUpdates.map {
                val stepInitialProgress = initialProgress

                initialProgress += maxProgressPerStep

                AvailableUpdateStep(it, stepInitialProgress, totalSteps)
            }
        }
    }
}

private const val FULL_PROGRESS = 100

/**
 * Step of an OTA update
 *
 * An OTA update is composed by [1-N] [AvailableUpdateStep], but we want the user to see the update
 * as a single process that flows from 0 to 100%. Thus, we need to limit the [MIN, MAX] progress
 * values each step displays to the user
 */
internal data class AvailableUpdateStep(
    val availableUpdate: AvailableUpdate,
    val startingProgress: Int,
    val progressDividend: Int
) {
    val typeName: String = availableUpdate.type.toString()
}
