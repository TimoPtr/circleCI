package com.kolibree.sdkws.core

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.sdkws.networking.Response
import java.net.HttpURLConnection.HTTP_OK
import org.junit.Test

class ForceAppUpdateCheckerImplTest : BaseUnitTest() {
    private val forceAppUpdateChecker = ForceAppUpdateCheckerImpl()

    @Test
    fun maybeNotifyForcedAppUpdate_responseNoError_neverEmits() {
        val observer = forceAppUpdateChecker.isAppUpdateNeeded().test()

        forceAppUpdateChecker.maybeNotifyForcedAppUpdate(Response(HTTP_OK, ""))

        observer.assertEmpty()
    }

    @Test
    fun maybeNotifyForcedAppUpdate_responseWithErrorDifferentThanUpgrade_neverEmits() {
        val observer = forceAppUpdateChecker.isAppUpdateNeeded().test()

        forceAppUpdateChecker.maybeNotifyForcedAppUpdate(Response(ApiError.generateNetworkError()))

        observer.assertValue(false)
    }

    @Test
    fun maybeNotifyForcedAppUpdate_responseWithErrorUpdateNeeded_emitsValue() {
        val observer = forceAppUpdateChecker.isAppUpdateNeeded().test()

        forceAppUpdateChecker.maybeNotifyForcedAppUpdate(
            Response(
                ApiError(
                    "",
                    ApiErrorCode.INVALID_APP_VERSION,
                    ""
                )
            )
        )

        observer.assertValue(true)
    }
}
