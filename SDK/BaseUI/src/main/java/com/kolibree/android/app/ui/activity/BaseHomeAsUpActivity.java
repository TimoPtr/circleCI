package com.kolibree.android.app.ui.activity;

import androidx.appcompat.widget.Toolbar;

/** Created by maragues on 06/04/16. */
public abstract class BaseHomeAsUpActivity extends BaseActivity {

  public void showToolbarUpButton(Toolbar toolbar) {
    // Toolbar
    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      toolbar.setNavigationOnClickListener(v -> finish());
    }
  }
}
