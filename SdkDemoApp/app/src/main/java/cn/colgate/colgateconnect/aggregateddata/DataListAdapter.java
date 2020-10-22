package cn.colgate.colgateconnect.aggregateddata;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.colgate.colgateconnect.R;
import com.kolibree.statsoffline.persistence.models.StatsSession;
import java.util.ArrayList;
import java.util.List;

public class DataListAdapter extends RecyclerView.Adapter {
  List<StatsSession> data = new ArrayList<>();

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_aggregated_data, parent, false);
    return new DataViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    DataViewHolder viewHolder = (DataViewHolder) holder;
    StatsSession session = data.get(position);
    viewHolder.tvProfileId.setText(String.valueOf(session.getProfileId()));
    viewHolder.tvCreationTime.setText(session.getCreationTime().toString());
    viewHolder.tvDuration.setText(String.valueOf(session.getDuration()));
    viewHolder.tvAverageSurface.setText(String.valueOf(session.getAverageSurface()));
    viewHolder.tvAverageCheckup.setText(String.valueOf(session.getAverageCheckup().size()));
    viewHolder.tvAssignedDate.setText(session.getAssignedDate().toString());
  }

  @Override
  public int getItemCount() {
    return data == null ? 0 : data.size();
  }

  public List<StatsSession> getData() {
    return data;
  }

  public void reloadData(List<StatsSession> data) {
    this.data.clear();
    this.data.addAll(data);
    notifyDataSetChanged();
  }

  private class DataViewHolder extends RecyclerView.ViewHolder {

    private TextView tvProfileId;
    private TextView tvCreationTime;
    private TextView tvDuration;
    private TextView tvAverageSurface;
    private TextView tvAverageCheckup;
    private TextView tvAssignedDate;

    public DataViewHolder(View itemView) {
      super(itemView);
      tvProfileId = (TextView) itemView.findViewById(R.id.profile_id);
      tvCreationTime = (TextView) itemView.findViewById(R.id.creation_time);
      tvDuration = (TextView) itemView.findViewById(R.id.duration);
      tvAverageSurface = (TextView) itemView.findViewById(R.id.average_surface);
      tvAverageCheckup = (TextView) itemView.findViewById(R.id.average_checkup);
      tvAssignedDate = (TextView) itemView.findViewById(R.id.assigned_date);
    }
  }
}
