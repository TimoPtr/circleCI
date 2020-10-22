package com.kolibree.android.app.dagger.scopes;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import javax.inject.Scope;

/** Created by miguelaragues on 17/7/17. */
@Scope
@Documented
@Retention(RUNTIME)
public @interface FragmentScope {}
