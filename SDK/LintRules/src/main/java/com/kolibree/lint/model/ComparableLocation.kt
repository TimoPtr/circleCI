/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.lint.model

import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Position
import java.io.File

internal class ComparableLocation(
    file: File,
    start: Position?,
    end: Position?
) : Location(file, start, end) {

    constructor(location: Location) : this(location.file, location.start, location.end)

    fun isEqualTo(other: ComparableLocation): Boolean =
        file == other.file && samePosition(start, other.start) && samePosition(end, other.end)

    private fun samePosition(lhs: Position?, rhs: Position?): Boolean = when {
        lhs == null && rhs == null -> true
        lhs == null && rhs != null -> false
        lhs != null && rhs == null -> false
        lhs != null && rhs != null -> lhs.sameLine(rhs)
        else -> false
    }
}
