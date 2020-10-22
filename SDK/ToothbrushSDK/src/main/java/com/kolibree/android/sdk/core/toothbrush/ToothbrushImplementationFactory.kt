package com.kolibree.android.sdk.core.toothbrush

import android.content.Context
import com.google.common.base.Optional
import com.kolibree.android.app.dagger.SingleThreadSchedulerModule
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.HUM_BATTERY
import com.kolibree.android.commons.ToothbrushModel.HUM_ELECTRIC
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.driver.BaseDriver
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.core.ota.kltb003.ToothbrushUpdaterModule
import com.kolibree.android.sdk.dagger.SdkComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Scope

/** [ToothbrushImplementation] factory  */
internal object ToothbrushImplementationFactory {

    @JvmStatic
    @Suppress("LongParameterList")
    fun createToothbrushImplementation(
        connection: InternalKLTBConnection,
        context: Context,
        driver: BaseDriver,
        mac: String,
        model: ToothbrushModel,
        name: String
    ): ToothbrushImplementation = when (model) {
        CONNECT_M1,
        CONNECT_E2,
        CONNECT_B1,
        PLAQLESS,
        HILINK,
        HUM_ELECTRIC,
        HUM_BATTERY,
        GLINT -> {
            DaggerToothbrushKLTB003Component.factory()
                .create(
                    sdkComponent = KolibreeAndroidSdk.getSdkComponent(),
                    context = context,
                    kltbConnection = connection,
                    bleDriver = driver as BleDriver,
                    mac = Optional.of(mac),
                    model = model,
                    name = name
                ).toothbrush()
        }
        ARA, CONNECT_E1 ->
            ToothbrushKLTB002Impl.create(
                Optional.of(mac),
                model,
                driver as BleDriver,
                name,
                connection
            )
    }
}

@KLTBConnectionScope
@Component(
    dependencies = [SdkComponent::class],
    modules = [ToothbrushUpdaterModule::class, SingleThreadSchedulerModule::class]
)
internal interface ToothbrushKLTB003Component {
    @Component.Factory
    interface Factory {
        fun create(
            sdkComponent: SdkComponent,
            @BindsInstance context: Context,
            @BindsInstance kltbConnection: InternalKLTBConnection,
            @BindsInstance bleDriver: BleDriver,
            @BindsInstance @ToothbrushMac mac: Optional<String>,
            @BindsInstance name: String,
            @BindsInstance model: ToothbrushModel
        ): ToothbrushKLTB003Component
    }

    fun toothbrush(): ToothbrushKLTB003Impl
}

@Scope
@Retention
internal annotation class KLTBConnectionScope
