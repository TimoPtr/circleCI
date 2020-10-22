package com.kolibree.android.extensions

import android.content.Intent
import androidx.annotation.Keep

@Keep
fun Intent.hasFlags(flags: Int) = this.flags and flags == flags
