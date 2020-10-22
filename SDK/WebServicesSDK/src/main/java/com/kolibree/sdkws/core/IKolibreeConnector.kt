package com.kolibree.sdkws.core

import androidx.annotation.Keep
import com.kolibree.sdkws.api.response.InstructionsResponse
import com.kolibree.sdkws.api.response.UpdateToothbrushResponse
import com.kolibree.sdkws.data.model.UpdateToothbrushData
import com.kolibree.sdkws.sms.SmsAccountManager
import io.reactivex.Observable
import io.reactivex.Single
import java.io.IOException
import java.util.ArrayList

@Keep
interface IKolibreeConnector : SmsAccountManager, AccountOperations, ProfileOperations {
    fun getDefaultAvatarList(callback: KolibreeConnectorListener<ArrayList<String>>)

    @Deprecated(message = "Use UpdateToothbrushUseCase instead")
    fun updateToothbrush(
        data: UpdateToothbrushData,
        callback: KolibreeConnectorListener<UpdateToothbrushResponse>
    )

    fun updateToothbrush(data: UpdateToothbrushData): Single<UpdateToothbrushResponse>

    fun getInstructions(callback: KolibreeConnectorListener<InstructionsResponse>)

    @Throws(IOException::class)
    fun getAvroFileUploadUrl(): String

    @Throws(IOException::class)
    fun getVideoUploadUrl(avroAmazonUrl: String): String

    val refreshObservable: Observable<Long>
    fun syncAndNotify(): Single<Boolean>
    val isAppUpdateNeeded: Observable<Boolean>
}
