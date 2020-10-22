package cn.colgate.colgateconnect.scan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.colgate.colgateconnect.R;
import com.kolibree.android.sdk.scan.ToothbrushScanResult;
import java.util.ArrayList;
import java.util.List;

/** Toothbrush adapter. Display the list of nearby toothbrushes discovered */
final class ToothbrushAdapter extends RecyclerView.Adapter<ToothbrushAdapter.ToothbrushHolder> {

  private final List<ToothbrushScanResult> data = new ArrayList<>();
  private final ScanView view;

  ToothbrushAdapter(ScanView view, @NonNull List<ToothbrushScanResult> data) {
    this.data.addAll(data);
    this.view = view;
  }

  @Override
  public ToothbrushHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ToothbrushHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_toothbrush, parent, false));
  }

  @Override
  public void onBindViewHolder(ToothbrushHolder holder, int position) {
    final ToothbrushScanResult result = data.get(position);
    holder.name.setText(result.getName());
    holder.mac.setText(result.getMac());
    holder.root.setOnClickListener(v -> view.onItemClick(result));
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  /** Toothbrush item views holder */
  class ToothbrushHolder extends RecyclerView.ViewHolder {

    private final TextView name;
    private final TextView mac;
    private final View root;

    private ToothbrushHolder(@NonNull View root) {
      super(root);
      this.root = root;

      name = root.findViewById(R.id.toothbrush_item_name);
      mac = root.findViewById(R.id.toothbrush_item_mac);
    }
  }
}
