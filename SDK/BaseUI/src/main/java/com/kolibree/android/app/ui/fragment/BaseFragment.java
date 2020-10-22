package com.kolibree.android.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.CallSuper;
import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.kolibree.android.auditor.Auditor;
import com.kolibree.android.tracker.Analytics;
import com.kolibree.android.tracker.NonTrackableScreen;
import com.kolibree.android.tracker.TrackableScreen;
import timber.log.Timber;

@Keep
public class BaseFragment extends Fragment {

  private static String TAG_LIFECYCLE = "FragmentLifecycle";

  private boolean isVisibleToUser;

  private Unbinder unbinder;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Timber.tag(TAG_LIFECYCLE).v("%s - onCreate", getClass().getSimpleName());
  }

  protected final View inflateView(LayoutInflater inflater, ViewGroup container, int layoutResId) {
    View view = inflater.inflate(layoutResId, container, false);
    bindView(view);
    return view;
  }

  private void bindView(View view) {
    if (unbinder != null) {
      unbinder.unbind();
    }

    unbinder = ButterKnife.bind(this, view);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    if (unbinder != null) {
      unbinder.unbind();
      unbinder = null;
    }
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Auditor.Companion.instance().notifyFragmentViewCreated(view, this, getActivity());
  }

  @Override
  public void onStart() {
    Timber.tag(TAG_LIFECYCLE).v("%s - onStart", getClass().getSimpleName());
    Auditor.Companion.instance().notifyFragmentStarted(this, getActivity());
    /*
     * This method will be called when viewpager creates fragment
     * and when we go to this fragment background or another activity or fragment
     * NOT called when we switch between each page in ViewPager
     */
    if (isVisibleToUser) {
      onVisible();
    }
    super.onStart();
  }

  @Override
  public void onResume() {
    Timber.tag(TAG_LIFECYCLE).v("%s - onResume", getClass().getSimpleName());
    onVisible();
    Auditor.Companion.instance().notifyFragmentResumed(this, getActivity());
    super.onResume();
  }

  @Override
  public void onPause() {
    Timber.tag(TAG_LIFECYCLE).v("%s - onPause", getClass().getSimpleName());
    Auditor.Companion.instance().notifyFragmentPaused(this, getActivity());
    super.onPause();
  }

  @Override
  public void onStop() {
    Timber.tag(TAG_LIFECYCLE).v("%s - onStop", getClass().getSimpleName());
    Auditor.Companion.instance().notifyFragmentStopped(this, getActivity());
    super.onStop();
  }

  @Override
  public void onDestroy() {
    Timber.tag(TAG_LIFECYCLE).v("%s - onDestroy", getClass().getSimpleName());
    super.onDestroy();
  }

  @CallSuper
  protected void onVisible() {
    boolean shouldTrack = !(this instanceof NonTrackableScreen);
    if (shouldTrack) {
      if (this instanceof TrackableScreen) {
        Analytics.setCurrentScreen(
            requireActivity(), ((TrackableScreen) this).getScreenName().getName());
      } else {
        String name = getClass().getSimpleName();
        Analytics.setCurrentScreen(requireActivity(), name);
      }
    }
  }
}
