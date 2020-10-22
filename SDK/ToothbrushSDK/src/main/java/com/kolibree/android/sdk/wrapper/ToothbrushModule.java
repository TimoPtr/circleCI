package com.kolibree.android.sdk.wrapper;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class ToothbrushModule {

  @Binds
  abstract ToothbrushFacade bindsToothbrushWrapper(ToothbrushFacadeImpl accountManager);
}
