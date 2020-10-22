package cn.colgate.colgateconnect.orphanbrushings;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ProfileListFragmentModule {

  @ContributesAndroidInjector
  abstract ProfileListDialogFragment contributeProfileListDialogFragment();
}
