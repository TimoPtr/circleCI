package cn.colgate.colgateconnect.aggregateddata;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.base.SdkDemoBaseActivity;
import com.kolibree.statsoffline.persistence.models.StatsSession;
import java.util.ArrayList;
import java.util.List;

/** To display statistic data in the list view */
public class AggregatedDataListActivity extends SdkDemoBaseActivity {
  public static String QUERY_DATA;

  @BindView(R.id.list_view)
  RecyclerView listView;

  private DataListAdapter adapter;

  private List<StatsSession> queryData;

  public static void start(Context context, List<StatsSession> queryData) {
    context.startActivity(
        new Intent(context, AggregatedDataListActivity.class)
            .putParcelableArrayListExtra(QUERY_DATA, (ArrayList<? extends Parcelable>) queryData));
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_aggregated_data_list);
    fetchQueryData();
    initListView();
  }

  private void fetchQueryData() {
    queryData = (List<StatsSession>) getIntent().getSerializableExtra(QUERY_DATA);
  }

  private void initListView() {
    if (queryData == null) {
      Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
      return;
    }
    LinearLayoutManager manager = new LinearLayoutManager(this);
    listView.setLayoutManager(manager);
    listView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    adapter = new DataListAdapter();
    listView.setAdapter(adapter);
    adapter.reloadData(queryData);
  }
}
