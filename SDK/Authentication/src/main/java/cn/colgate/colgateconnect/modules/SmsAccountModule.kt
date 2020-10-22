package cn.colgate.colgateconnect.modules

import android.annotation.SuppressLint
import com.kolibree.sdkws.sms.SmsAccountManager
import com.kolibree.sdkws.sms.SmsAccountManagerImpl
import dagger.Binds
import dagger.Module

/**
 * Created by Guillaume Agis on 10/10/2018.
 */
@SuppressLint("SdkPublicClassInNonKolibreePackage")
@Module
abstract class SmsAccountModule {
    @Binds
    internal abstract fun bindsSmsAccountManager(implementation: SmsAccountManagerImpl): SmsAccountManager
}
