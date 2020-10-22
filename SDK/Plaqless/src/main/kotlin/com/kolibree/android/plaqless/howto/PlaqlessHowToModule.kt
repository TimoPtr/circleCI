/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto

import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.plaqless.howto.intro.PlaqlessHowToNavigator
import com.kolibree.android.plaqless.howto.intro.PlaqlessIntroFragment
import com.kolibree.android.plaqless.howto.intro.slide1.SlideOneFragment
import com.kolibree.android.plaqless.howto.intro.slide2.SlideTwoFragment
import com.kolibree.android.plaqless.howto.intro.slide3.SlideThreeFragment
import com.kolibree.android.plaqless.howto.intro.slides.SlidesFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PlaqlessHowToModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [PlaqlessNavigatorModule::class])
    internal abstract fun contributePlaqlessIntroFragment(): PlaqlessIntroFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [PlaqlessNavigatorModule::class])
    internal abstract fun contributeSlidesFragment(): SlidesFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeSlideOneFragment(): SlideOneFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeSlideTwoFragment(): SlideTwoFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [PlaqlessNavigatorModule::class])
    internal abstract fun contributeSlideThreeFragment(): SlideThreeFragment
}

@Module
internal abstract class PlaqlessNavigatorModule {

    @Binds
    abstract fun bindsPlaqlessHowToNavigator(activity: PlaqlessHowToActivity): PlaqlessHowToNavigator
}
