/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kolibree.android.baseui.v1.R;
import java.util.ArrayList;

/** Created by aurelien on 13/10/15. */
public final class OptionDialogFragment extends KolibreeDialogFragment
    implements AdapterView.OnItemClickListener {

  private static final String ARG_TITLE = "title";
  private static final String ARG_OPTIONS = "options";
  private static final String ARG_DIALOG_ID = "dialog_id";

  /**
   * Create an OptionDialogFragment that offers the user a list of options to choose from.
   *
   * @param title title
   * @param options the list of options
   * @param dialogId dialogId
   * @return a non-null OptionDialogFragment
   */
  @NonNull
  public static OptionDialogFragment newInstance(
      String title, ArrayList<String> options, int dialogId) {
    final OptionDialogFragment dialog = new OptionDialogFragment();
    final Bundle arguments = new Bundle();

    arguments.putString(ARG_TITLE, title);
    arguments.putStringArrayList(ARG_OPTIONS, options);
    arguments.putInt(ARG_DIALOG_ID, dialogId);
    dialog.setArguments(arguments);

    return dialog;
  }

  public static OptionDialogFragment newInstance(String title, ArrayList<String> options) {
    return newInstance(title, options, 0);
  }

  @Override
  public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
    final String title = getArguments().getString(ARG_TITLE);
    final ArrayList<String> options = getArguments().getStringArrayList(ARG_OPTIONS);

    // Title
    setTitle(title);

    // List
    final ListView list = v.findViewById(R.id.dialog_list);
    list.setOnItemClickListener(this);
    list.setAdapter(new OptionAdapter(getActivity(), options));
  }

  @Override
  protected int getContentLayout() {
    return R.layout.dialog_option;
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    final String value = (String) adapterView.getAdapter().getItem(i);
    final int dialogId = getArguments().getInt(ARG_DIALOG_ID);
    ((OptionDialogCallback) (getTargetFragment() != null ? getTargetFragment() : getActivity()))
        .onOptionSelected(i, value, dialogId);
    dismiss();
  }

  /** To be implemented by caller Fragment or Activity. */
  public interface OptionDialogCallback {

    void onOptionSelected(int index, String value, int dialogId);
  }

  private static final class OptionAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> data;

    private OptionAdapter(Context context, ArrayList<String> options) {
      this.context = context;
      this.data = options;
    }

    @Override
    public int getCount() {
      return data.size();
    }

    @Override
    public String getItem(int i) {
      return data.get(i);
    }

    @Override
    public long getItemId(int i) {
      return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
      Holder holder;

      if (view == null) {
        view = LayoutInflater.from(context).inflate(R.layout.item_option_dialog, null);
        holder = new Holder(view);
        view.setTag(holder);
      } else {
        holder = (Holder) view.getTag();
      }

      holder.itemName.setText(getItem(i));

      return view;
    }

    private static class Holder {

      private final TextView itemName;

      private Holder(View v) {
        itemName = v.findViewById(R.id.item_name);
      }
    }
  }
}
