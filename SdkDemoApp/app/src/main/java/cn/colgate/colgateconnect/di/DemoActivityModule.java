package cn.colgate.colgateconnect.di;

import cn.colgate.colgateconnect.aggregateddata.AggregatedDataListActivity;
import cn.colgate.colgateconnect.home.HeaderActivity;
import cn.colgate.colgateconnect.home.MainActivity;
import cn.colgate.colgateconnect.login.LoginActivity;
import cn.colgate.colgateconnect.orphanbrushings.OrphanBrushingsActivity;
import cn.colgate.colgateconnect.orphanbrushings.OrphanBrushingsActivityModule;
import cn.colgate.colgateconnect.orphanbrushings.ProfileListFragmentModule;
import cn.colgate.colgateconnect.profile.ProfilesListActivity;
import cn.colgate.colgateconnect.register.ProfileCreatedActivity;
import cn.colgate.colgateconnect.register.RegisterActivity;
import cn.colgate.colgateconnect.scan.ScanActivity;
import cn.colgate.colgateconnect.toothbrush.ToothbrushActivity;
import cn.colgate.colgateconnect.wxapi.WXEntryActivity;
import com.kolibree.android.offlinebrushings.di.ExtractOfflineBrushingsModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/** Add here all the activities that will use the @inject annotation */
@Module
abstract class DemoActivityModule {

  @ContributesAndroidInjector(
      modules = {DemoFragmentsModule.class, ExtractOfflineBrushingsModule.class})
  // include the fragments associated to this activity that will use the @inject annotation
  abstract MainActivity contributeMainActivityInjector();

  @ContributesAndroidInjector
  abstract HeaderActivity contributeHeaderActivityInjector();

  @ContributesAndroidInjector
  abstract ScanActivity contributeScanActivityInjector();

  @ContributesAndroidInjector
  abstract LoginActivity contributeLoginActivityInjector();

  @ContributesAndroidInjector
  abstract RegisterActivity contributeRegisterActivityInjector();

  @ContributesAndroidInjector
  abstract ProfileCreatedActivity contributeProfileCreatedActivityInjector();

  @ContributesAndroidInjector(modules = ProfileListFragmentModule.class)
  abstract ToothbrushActivity contributeTBActivityInjector();

  @ContributesAndroidInjector
  abstract ProfilesListActivity contributeProfileListActivityInjector();

  @ContributesAndroidInjector(
      modules = {ProfileListFragmentModule.class, OrphanBrushingsActivityModule.class})
  abstract OrphanBrushingsActivity contributeOrphanBrushingsActivity();

  @ContributesAndroidInjector
  abstract WXEntryActivity contributeWXEntryActivity();

  @ContributesAndroidInjector
  abstract AggregatedDataListActivity contributeAggregatedDataListActivity();
}
