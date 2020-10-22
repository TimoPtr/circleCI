package com.kolibree.android.app.dagger;

import com.kolibree.android.app.async.AppClearUserContentJobService;
import com.kolibree.android.app.dagger.scopes.ActivityScope;
import com.kolibree.android.app.sdk.InjectionTestActivity;
import com.kolibree.android.app.ui.settings.secret.dialogs.DialogsPlaygroundActivity;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/** Created by miguelaragues on 14/9/17. */
@SuppressWarnings("KotlinInternalInJava")
@Module
abstract class EspressoBindingModule {

  @ActivityScope
  @ContributesAndroidInjector
  abstract AppClearUserContentJobService bindClearUserContentJobService();

  @ActivityScope
  @ContributesAndroidInjector
  abstract DialogsPlaygroundActivity bindDialogPlaygroundActivity();

  @ActivityScope
  @ContributesAndroidInjector
  abstract InjectionTestActivity bindInjectionTestActivity();
}
