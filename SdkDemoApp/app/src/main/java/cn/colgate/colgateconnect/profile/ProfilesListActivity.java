package cn.colgate.colgateconnect.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.base.SdkDemoBaseActivity;
import cn.colgate.colgateconnect.demo.AccountInfo;
import cn.colgate.colgateconnect.demo.DataStore;
import cn.colgate.colgateconnect.home.MainActivity;
import cn.colgate.colgateconnect.model.DemoProfile;
import com.kolibree.account.AccountFacade;
import com.kolibree.account.ProfileFacade;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.commons.profile.Gender;
import com.kolibree.android.commons.profile.Handedness;
import com.kolibree.sdkws.brushing.wrapper.IBrushing;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import org.threeten.bp.LocalDate;

/** Created by Guillaume Agis on 15/11/2018. */
public class ProfilesListActivity extends SdkDemoBaseActivity {

  @BindView(R.id.tvList)
  ListView tvList;

  @BindView(R.id.loading)
  ProgressBar loading;

  @Inject DataStore dataStore;

  // profile module
  @Inject AccountFacade accountFacade;

  @Inject ProfileFacade profileFacade;

  @Inject AccountInfo accountInfo;

  public static void start(Context context) {
    Intent starter = new Intent(context, ProfilesListActivity.class);
    context.startActivity(starter);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile_list);
    displayBackButtonToolbar();
    initView();
  }

  private void initView() {
    ArrayList<String> userNames = new ArrayList<>();
    List<IProfile> profiles = accountInfo.getProfiles();
    for (int i = 0; i < profiles.size(); i++) {
      userNames.add(profiles.get(i).getFirstName());
    }

    ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNames);
    tvList.setAdapter(adapter);
    tvList.setOnItemClickListener(
        (parent, view, position, id) -> {
          IProfile profileSelected = profiles.get(position);

          accountInfo.setCurrentProfile(profileSelected);
          profileFacade.setActiveProfile(profileSelected.getId());
          dataStore.storeProfile(
              profileSelected); // store the profile locally, used when quitting the app and coming
          // back to the right profile.
          // you ned to make sure the app reload the data every time you open it given the current
          // profile.
          MainActivity.start(this);
          finish();

          Toast.makeText(
                  getBaseContext(),
                  "Profile switched to " + profileSelected.getFirstName(),
                  Toast.LENGTH_SHORT)
              .show();
        });
  }

  /** Will create a new profile and add it to the account */
  @OnClick(R.id.tvAddProfile)
  public void createRandomProfile() {
    List<Integer> brushingGoalTimes = IBrushing.Companion.brushingGoalTimes();
    Random rand = new Random();

    int n = rand.nextInt(70) + 18;

    LocalDate birthday = LocalDate.of(2018 - n, 12, 12);
    // create a new profile with picture
    DemoProfile newProfile =
        new DemoProfile(
            (long) n,
            "John (" + TrustedClock.getNowLocalDate().toString() + ")",
            Gender.MALE,
            Handedness.LEFT_HANDED,
            brushingGoalTimes.get(rand.nextInt(brushingGoalTimes.size())),
            TrustedClock.getNowLocalDateTime().toString(),
            birthday,
            "https://cdn3.iconfinder.com/data/icons/avatars-15/64/_Modern_Man-512.png",
            "");
    loading.setVisibility(View.VISIBLE);
    disposables.add(
        profileFacade
            .createProfile(newProfile)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(profiles -> getProfileslist(), this::error));
  }

  private void error(Throwable throwable) {
    throwable.printStackTrace();
    loading.setVisibility(View.GONE);
  }

  /** Reload the view while getting the new list of profiles */
  private void getProfileslist() {
    disposables.add(
        profileFacade
            .getProfilesList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                profiles -> {
                  loading.setVisibility(View.GONE);
                  accountInfo.setProfiles(profiles);
                  initView();
                },
                this::error));
  }
}
