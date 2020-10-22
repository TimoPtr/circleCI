package com.kolibree.android.translationssupport

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.Keep
import com.jcminarro.philology.Philology
import io.github.inflationx.viewpump.ViewPumpContextWrapper

@Keep
class TranslationContext(context: Context) : ContextWrapper(ViewPumpContextWrapper.wrap(Philology.wrap(context)))
