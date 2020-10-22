package cn.colgate.colgateconnect.modules;

import android.annotation.SuppressLint;
import dagger.Module;

@SuppressLint("SdkPublicClassInNonKolibreePackage")
@Module(includes = {AuthMethodModule.class})
public abstract class AuthFragmentsModule {}
