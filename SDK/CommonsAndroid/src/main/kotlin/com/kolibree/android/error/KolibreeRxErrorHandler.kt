package com.kolibree.android.error

import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly
import io.reactivex.functions.Consumer
import javax.inject.Inject

@Keep
class KolibreeRxErrorHandler @Inject constructor() : Consumer<Throwable> {

    override fun accept(exception: Throwable) {
        FailEarly.fail(
            tag = "KolibreeRxErrorHandler",
            message = "Caught unhandled RX exception!",
            exception = exception
        )
    }
}
