/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package cn.colgate.colgateconnect.orphanbrushings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.demo.AccountInfo;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.app.ui.fragment.BaseDaggerDialogFragment;
import java.util.List;
import javax.inject.Inject;

/** Created by miguelaragues on 20/10/17. */
public class ProfileListDialogFragment extends BaseDaggerDialogFragment
    implements OnItemClickListener {

  public static final String TAG = ProfileListDialogFragment.class.getSimpleName();
  public static final int ID_SHARED_TOOTHBRUSH = -2;
  private static final String EXTRA_SHOW_SHARED_TEXT = "extra_show_shared";
  ListView profileList;
  @Inject AccountInfo accountInfo;
  private ProfileAdapter adapter;
  private boolean showSharedText;

  /**
   * Show a ProfileListDialogFragment if it's not already displayed.
   *
   * @param fragmentManager the fragment manager to be used
   * @return a non-null ProfileListDialogFragment. The returned instance is not necessarily
   *     displayed
   */
  @NonNull
  private static ProfileListDialogFragment showIfNotPresent(
      FragmentManager fragmentManager, boolean withSharedText) {
    ProfileListDialogFragment dialogFragment =
        (ProfileListDialogFragment) fragmentManager.findFragmentByTag(TAG);
    if (dialogFragment == null) {
      dialogFragment = new ProfileListDialogFragment();

      Bundle bundle = new Bundle();
      bundle.putBoolean(EXTRA_SHOW_SHARED_TEXT, withSharedText);
      dialogFragment.setArguments(bundle);

      dialogFragment.showNow(fragmentManager, TAG);
    }

    return dialogFragment;
  }

  @NonNull
  public static ProfileListDialogFragment showWithSharedText(FragmentManager fragmentManager) {
    return showIfNotPresent(fragmentManager, true);
  }

  @NonNull
  public static ProfileListDialogFragment showWithProfiles(FragmentManager fragmentManager) {
    return showIfNotPresent(fragmentManager, false);
  }

  @Override
  protected int getContentLayout() {
    return R.layout.dialog_profile_list;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setTitle(R.string.profile_list_dialog_title);

    profileList = view.findViewById(R.id.profile_dialog_list);

    showSharedText = getArguments().getBoolean(EXTRA_SHOW_SHARED_TEXT, false);

    adapter = new ProfileAdapter(getContext(), accountInfo.getProfiles(), showSharedText);
    profileList.setAdapter(adapter);
    profileList.setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    long profileId;
    if (position >= accountInfo.getProfiles().size()) {
      profileId = ID_SHARED_TOOTHBRUSH;
    } else {
      profileId = adapter.getItem(position).getId();
    }

    ((ProfileSelectorCallback) getActivity()).onProfileSelected(profileId);

    dismiss();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);

    Log.d(getTag(), "On Cancel");
  }

  public interface ProfileSelectorCallback extends DialogInterface.OnCancelListener {

    /** @param profileId the selected profile id or ID_SHARED_TOOTHBRUSH if user selected shared */
    void onProfileSelected(long profileId);
  }

  private static class ProfileAdapter extends ArrayAdapter<IProfile> {

    private final boolean showSharedText;

    ProfileAdapter(Context context, List<IProfile> profileList, boolean showSharedText) {
      super(context, 0, profileList);

      this.showSharedText = showSharedText;
    }

    @Override
    public int getCount() {
      if (showSharedText) {
        return super.getCount() + 1;
      }

      return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      ViewHolder holder;
      if (convertView == null) {
        convertView =
            LayoutInflater.from(getContext()).inflate(R.layout.item_profile, parent, false);

        holder = new ViewHolder(convertView);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      if (position < super.getCount()) {
        final IProfile p = getItem(position);

        holder.name.setText(p.getFirstName());
      } else {
        holder.name.setText("Shared Toothbrush");
      }

      return convertView;
    }

    static class ViewHolder {

      private final TextView name;

      private ViewHolder(View v) {
        name = v.findViewById(R.id.profile_name);
      }
    }
  }
}
