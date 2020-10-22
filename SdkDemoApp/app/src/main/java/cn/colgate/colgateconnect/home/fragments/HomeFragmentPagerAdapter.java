package cn.colgate.colgateconnect.home.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/** Home viewpager adapter to display the nav bar */
public final class HomeFragmentPagerAdapter extends FragmentStatePagerAdapter {

  private static final int PROFILE_FRAGMENT_INDEX = 0;
  private static final int BRUSHING_FRAGMENT_INDEX = 1;
  private static final int CHECKUP_FRAGMENT_INDEX = 2;
  private static final int AGGREGATED_DATA_FRAGMENT_INDEX = 3;
  private static final int FRAGMENT_COUNT = 4;

  public HomeFragmentPagerAdapter(@NonNull FragmentManager fm) {
    super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
  }

  @NonNull
  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case PROFILE_FRAGMENT_INDEX:
        return new ProfileFragment();
      case BRUSHING_FRAGMENT_INDEX:
        return new BrushingFragment();
      case CHECKUP_FRAGMENT_INDEX:
        return new CheckupFragment();
      case AGGREGATED_DATA_FRAGMENT_INDEX:
        return new AggregatedDataFragment();
    }

    throw new IllegalStateException();
  }

  @Override
  public int getCount() {
    return FRAGMENT_COUNT;
  }
}
