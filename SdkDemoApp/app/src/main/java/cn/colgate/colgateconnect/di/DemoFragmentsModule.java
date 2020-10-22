package cn.colgate.colgateconnect.di;

import cn.colgate.colgateconnect.home.fragments.AggregatedDataFragment;
import cn.colgate.colgateconnect.home.fragments.BrushingFragment;
import cn.colgate.colgateconnect.home.fragments.CheckupFragment;
import cn.colgate.colgateconnect.home.fragments.ProfileFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/** Add here all the fragments that will use the @inject annotation used in the HomePageActivity */
@Module
abstract class DemoFragmentsModule {

  @ContributesAndroidInjector
  abstract CheckupFragment contributeCheckupFragment();

  @ContributesAndroidInjector
  abstract ProfileFragment contributeProfileFragment();

  @ContributesAndroidInjector
  abstract BrushingFragment contributeBrushingFragment();

  @ContributesAndroidInjector
  abstract AggregatedDataFragment contributeAggregatedDataFragment();
}
