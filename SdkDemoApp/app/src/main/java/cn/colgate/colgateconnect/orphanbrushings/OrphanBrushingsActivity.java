package cn.colgate.colgateconnect.orphanbrushings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import cn.colgate.colgateconnect.R;
import com.kolibree.android.app.ui.activity.BaseActivity;
import com.kolibree.android.offlinebrushings.OrphanBrushing;
import com.kolibree.android.offlinebrushings.OrphanBrushingMapper;
import com.kolibree.android.offlinebrushings.persistence.SDKOrphanBrushingRepository;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/** Created by miguelaragues on 19/10/17. */
public class OrphanBrushingsActivity extends BaseActivity
    implements HasAndroidInjector, ProfileListDialogFragment.ProfileSelectorCallback {

  private final CompositeDisposable disposables = new CompositeDisposable();
  private final CompositeDisposable adapterDisposables = new CompositeDisposable();

  @BindView(R.id.orphan_brushings_list)
  RecyclerView orphanRecyclerView;

  @BindView(R.id.orphan_brushings_list_empty)
  TextView orphanListEmpty;

  @Inject SDKOrphanBrushingRepository orphanBrushingRepository;
  @Inject OrphanBrushingMapper orphanBrushingMapper;
  @Inject DispatchingAndroidInjector<Object> fragmentInjector;
  private OrphanBrushingsAdapter adapter;
  private List<OrphanBrushing> orphanBrushings;
  private OrphanBrushing actionOrphanBrushing;

  public static void start(Context context) {
    Intent starter = new Intent(context, OrphanBrushingsActivity.class);
    context.startActivity(starter);
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return fragmentInjector;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_orphan_brushings);

    initRecyclerView();
  }

  @Override
  protected void onResume() {
    super.onResume();

    disposables.add(orphanBrushingRepository.readAll().subscribe(this::renderBrushings));
  }

  @Override
  protected void onPause() {
    super.onPause();

    disposables.clear();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    disposables.dispose();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      default:
    }

    return super.onOptionsItemSelected(item);
  }

  private void showSelectProfile() {
    ProfileListDialogFragment.showWithProfiles(getSupportFragmentManager());
  }

  private void renderBrushings(List<OrphanBrushing> brushings) {
    if (orphanBrushings == null) {
      adapter = new OrphanBrushingsAdapter(brushings);
      orphanRecyclerView.setAdapter(adapter);

      listenToOrphanBrushingClick(adapter);
    } else {
      DiffUtil.DiffResult diffResult =
          DiffUtil.calculateDiff(
              new OrphanBrushingsDiffUtilCallback(orphanBrushings, brushings), true);

      /*
      this is already in the UI thread, but diffutil does weird stuff and it's safer to do it like
      this

      See https://medium.com/@jonfhancock/get-threading-right-with-diffutil-423378e126d2
       */
      runOnUiThread(
          () -> {
            diffResult.dispatchUpdatesTo(adapter);
            adapter.setOrphanBrushings(brushings);
          });
    }

    orphanBrushings = brushings;

    orphanListEmpty.setVisibility(orphanBrushings.isEmpty() ? View.VISIBLE : View.GONE);
  }

  private void listenToOrphanBrushingClick(OrphanBrushingsAdapter adapter) {
    adapterDisposables.clear();

    adapterDisposables.add(
        adapter
            .clickedDeleteObservable()
            .subscribe(this::onUserWantsToDeleteOrphanBrushing, Throwable::printStackTrace));

    adapterDisposables.add(
        adapter
            .clickedAssignObservable()
            .subscribe(this::onUserWantsToAssignOrphanBrushing, Throwable::printStackTrace));

    disposables.add(adapterDisposables);
  }

  private void onUserWantsToAssignOrphanBrushing(OrphanBrushing orphanBrushing) {
    actionOrphanBrushing = orphanBrushing;

    showSelectProfile();
  }

  private void onUserWantsToDeleteOrphanBrushing(OrphanBrushing orphanBrushing) {
    disposables.add(
        orphanBrushingMapper
            .delete(Collections.singletonList(orphanBrushing))
            .subscribeOn(Schedulers.io())
            .subscribe(() -> {}, Throwable::printStackTrace));
  }

  private void initRecyclerView() {
    orphanRecyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  @Override
  public void onProfileSelected(long profileId) {
    if (actionOrphanBrushing == null) {
      Toast.makeText(
              getApplicationContext(), "Unexpected actionOrphanBrushing null", Toast.LENGTH_LONG)
          .show();
    }

    disposables.add(
        orphanBrushingMapper
            .assign(profileId, Collections.singletonList(actionOrphanBrushing))
            .subscribeOn(Schedulers.io())
            .subscribe(() -> {}, Throwable::printStackTrace));

    actionOrphanBrushing = null;
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    actionOrphanBrushing = null;
  }

  private static class OrphanBrushingsDiffUtilCallback extends DiffUtil.Callback {

    private List<OrphanBrushing> oldList;
    private List<OrphanBrushing> newList;

    public OrphanBrushingsDiffUtilCallback(
        List<OrphanBrushing> oldList, List<OrphanBrushing> newList) {
      this.oldList = oldList;
      this.newList = newList;
    }

    @Override
    public int getOldListSize() {
      return oldList.size();
    }

    @Override
    public int getNewListSize() {
      return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      // since they are immutable, the same code serves
      return areItemsTheSame(oldItemPosition, newItemPosition);
    }
  }
}
