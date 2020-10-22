package cn.colgate.colgateconnect.orphanbrushings;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.colgate.colgateconnect.R;
import com.kolibree.android.offlinebrushings.OrphanBrushing;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

/** Created by miguelaragues on 19/10/17. */
class OrphanBrushingsAdapter
    extends RecyclerView.Adapter<OrphanBrushingsAdapter.OrphanBrushingViewHolder> {

  private final List<OrphanBrushing> orphanBrushings = new ArrayList<>();
  DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy 年 MM月dd日, hh:mm下午").withZone(ZoneId.systemDefault());
  private PublishSubject<OrphanBrushing> deleteSubject = PublishSubject.create();
  private PublishSubject<OrphanBrushing> assignSubject = PublishSubject.create();

  OrphanBrushingsAdapter(List<OrphanBrushing> orphanBrushings) {
    this.orphanBrushings.addAll(orphanBrushings);

    setHasStableIds(true);
  }

  Observable<OrphanBrushing> clickedDeleteObservable() {
    return deleteSubject.hide();
  }

  Observable<OrphanBrushing> clickedAssignObservable() {
    return assignSubject.hide();
  }

  @Override
  public OrphanBrushingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new OrphanBrushingViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_orphan_brushing, parent, false));
  }

  @Override
  public void onBindViewHolder(OrphanBrushingViewHolder holder, int position) {
    holder.render(orphanBrushings.get(position));

    holder.delete.setOnClickListener(
        v -> deleteSubject.onNext(orphanBrushings.get(holder.getAdapterPosition())));

    holder.assign.setOnClickListener(
        v -> assignSubject.onNext(orphanBrushings.get(holder.getAdapterPosition())));
  }

  @Override
  public int getItemCount() {
    return orphanBrushings.size();
  }

  @Override
  public long getItemId(int position) {
    return orphanBrushings.get(position).getTimestamp();
  }

  void setOrphanBrushings(List<OrphanBrushing> orphanBrushings) {
    this.orphanBrushings.clear();
    this.orphanBrushings.addAll(orphanBrushings);
  }

  class OrphanBrushingViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.orphan_brushing_title)
    TextView titleView;

    @BindView(R.id.orphan_brushing_subtitle)
    TextView subtitleView;

    @BindView(R.id.orphan_brushing_delete)
    ImageButton delete;

    @BindView(R.id.orphan_brushing_assign)
    ImageButton assign;

    OrphanBrushingViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    void render(OrphanBrushing orphanBrushing) {
      titleView.setText(
          dateTimeFormatter.format(Instant.ofEpochMilli(orphanBrushing.getTimestamp())));

      setSubtitle(orphanBrushing);
    }

    @SuppressLint("DefaultLocale")
    private void setSubtitle(OrphanBrushing brushing) {
      long durationInSeconds = brushing.getDuration();
      long minutes = TimeUnit.SECONDS.toMinutes(durationInSeconds);
      long remainderSeconds =
          TimeUnit.SECONDS.toSeconds(durationInSeconds - TimeUnit.MINUTES.toSeconds(minutes));

      String duration = "Duration: %1$s min %2$s s";

      subtitleView.setText(
          String.format(
              duration,
              String.valueOf(minutes),
              String.format(Locale.getDefault(), "%02d", remainderSeconds)));
    }
  }
}
