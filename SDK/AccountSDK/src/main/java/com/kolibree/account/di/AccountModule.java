package com.kolibree.account.di;

import com.kolibree.account.eraser.ClearUserContentJobService;
import com.kolibree.account.eraser.UserSessionManager;
import com.kolibree.account.eraser.UserSessionManagerImpl;
import com.kolibree.account.logout.LogoutEnforcer;
import com.kolibree.account.logout.LogoutEnforcerImpl;
import com.kolibree.account.logout.ShouldLogoutUseCase;
import com.kolibree.account.logout.ShouldLogoutUseCaseImpl;
import com.kolibree.account.utils.ToothbrushForgetter;
import com.kolibree.account.utils.ToothbrushForgetterImpl;
import com.kolibree.account.utils.ToothbrushForgottenHook;
import com.kolibree.account.utils.ToothbrushesForProfileUseCase;
import com.kolibree.account.utils.ToothbrushesForProfileUseCaseImpl;
import com.kolibree.android.commons.interfaces.Truncable;
import com.kolibree.android.commons.interfaces.UserLogoutHook;
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository;
import com.kolibree.android.sdk.scan.ConnectionScannedTracker;
import com.kolibree.sdkws.core.OnUserLoggedInCallback;
import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoSet;
import dagger.multibindings.Multibinds;
import java.util.Set;

@Module(includes = {FacadeModule.class})
public abstract class AccountModule {

  @ContributesAndroidInjector
  abstract ClearUserContentJobService bindClearUserContentJobService();

  @Binds
  abstract OnUserLoggedInCallback bindsOnUserLoggedInCallback(LogoutEnforcerImpl impl);

  @Binds
  abstract LogoutEnforcer bindsLogoutEnforcer(LogoutEnforcerImpl impl);

  @Binds
  abstract ShouldLogoutUseCase bindsShouldLogoutUseCase(ShouldLogoutUseCaseImpl impl);

  @Binds
  abstract UserSessionManager bindsUserErasercontent(UserSessionManagerImpl userContentEraser);

  @Binds
  abstract ToothbrushesForProfileUseCase bindsToothbrushesForProfileUseCase(
      ToothbrushesForProfileUseCaseImpl impl);

  /*
  Support for apps with empty hooks, such as SdkDemo, JZ app or BtTester
   */
  @Multibinds
  abstract Set<ToothbrushForgottenHook> forgottenHookSet();

  @Binds
  abstract ToothbrushForgetter bindsToothbrushForgetter(ToothbrushForgetterImpl impl);

  /*
   * Dagger does not support a component dependency to add elements into another component's Set
   *
   * Until this feature is added, we need to do it manually :(
   *
   * https://github.com/google/dagger/issues/1112
   */
  @Binds
  @IntoSet
  abstract Truncable bindsToothbrushRepositoryAsTruncable(
      ToothbrushRepository toothbrushRepository);

  @Binds
  @IntoSet
  abstract Truncable bindsConnectionScannedTrackerAsTruncable(
      ConnectionScannedTracker connectionScannedTracker);

  // Support for apps with empty hooks
  @Multibinds
  abstract Set<UserLogoutHook> bindDefaultUserLogoutHook();
}
