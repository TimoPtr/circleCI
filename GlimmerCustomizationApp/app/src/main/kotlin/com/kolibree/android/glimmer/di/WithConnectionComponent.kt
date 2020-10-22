package com.kolibree.android.glimmer.di

import com.kolibree.android.sdk.connection.KLTBConnection
import dagger.BindsInstance
import dagger.Subcomponent
import dagger.android.DispatchingAndroidInjector

@Subcomponent(
    modules = [
        WithConnectionBindingModule::class
    ]
)
@WithConnectionScope
interface WithConnectionComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance connection: KLTBConnection
        ): WithConnectionComponent
    }

    fun connection(): KLTBConnection

    fun androidInjector(): DispatchingAndroidInjector<Any>
}
