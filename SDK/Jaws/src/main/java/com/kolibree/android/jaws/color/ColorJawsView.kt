package com.kolibree.android.jaws.color

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.kolibree.android.app.dagger.viewInjectorForViewType
import com.kolibree.android.jaws.R
import com.kolibree.android.jaws.base.BaseJawsView
import com.kolibree.android.jaws.tilt.gyroscopic.GyroscopicJawsTiltController
import javax.inject.Inject

@Keep
class ColorJawsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseJawsView<ColorJawsRenderer>(context, attrs) {

    @Inject
    override lateinit var renderer: ColorJawsRenderer

    @Inject
    internal lateinit var gyroscopicJawsTiltController: GyroscopicJawsTiltController

    init {
        context.viewInjectorForViewType<ColorJawsView>().inject(this)

        if (::renderer.isInitialized) {
            super.setRenderer(renderer)

            if (::gyroscopicJawsTiltController.isInitialized) {
                renderer.setTiltController(gyroscopicJawsTiltController)
            }
        }

        setNeglectedZoneColor(ContextCompat.getColor(context, R.color.neglectedZoneColor))
        setCleanZoneColor(ContextCompat.getColor(context, R.color.cleanZoneColor))
    }

    fun openJaws() = renderer.openAnimation()

    fun closeJaws() = renderer.closeAnimation()
}
