package cn.colgate.colgateconnect.home.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.base.BaseViewPagerFragment;
import cn.colgate.colgateconnect.demo.AccountInfo;
import cn.colgate.colgateconnect.demo.DataStore;
import cn.colgate.colgateconnect.home.MainActivity;
import cn.colgate.colgateconnect.login.LoginActivity;
import cn.colgate.colgateconnect.wxapi.WXApiManager;
import cn.colgate.colgateconnect.wxapi.WeChatEvent;
import com.kolibree.account.AccountFacade;
import com.kolibree.account.ProfileFacade;
import com.kolibree.account.WeChatData;
import com.kolibree.account.phone.PhoneNumberLink;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.network.api.ApiError;
import com.kolibree.charts.DashboardCalculatorView;
import com.kolibree.charts.models.WeeklyStat;
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.Locale;
import javax.inject.Inject;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;

/** Display the first tab with the stats */
public class ProfileFragment extends BaseViewPagerFragment {

  @BindView(R.id.tvNumberBrushings)
  TextView tvNumberBrushings;

  @BindView(R.id.tvAverageBrushingDuration)
  TextView tvAverageBrushingDuration;

  @BindView(R.id.tvAverageBrushingSurface)
  TextView tvAverageBrushingSurface;

  @BindView(R.id.confirmation_panel)
  View confirmationPanel;

  @BindView(R.id.phone_number)
  EditText phoneNumberEdit;

  @BindView(R.id.verification_code)
  EditText verificationCodeEdit;

  @BindView(R.id.wc_code)
  TextView wcCodeText;

  @Inject BrushingFacade brushingFacade;

  // brushing module
  @Inject BrushingFacade brushingManager;

  @Inject AccountFacade accountFacade;

  @Inject ProfileFacade profileFacade;

  // your profile data container, where you will store the data of the user for this session
  @Inject AccountInfo accountInfo;

  // stat module
  @Inject DashboardCalculatorView dashboardCalculatorView;

  @Inject DataStore dataStore;

  private Flowable<IProfile> activeProfileFlowable;
  private PhoneNumberLink phoneNumberLink;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    EventBus.getDefault().register(this);
    View v = inflateView(inflater, container, R.layout.fragment_profile);
    v.findViewById(R.id.unlink_phone).setOnClickListener(v1 -> unlinkPhoneNumber());
    v.findViewById(R.id.verify).setOnClickListener(v1 -> sendSmsCode());
    v.findViewById(R.id.link).setOnClickListener(v1 -> linkPhoneNumber());
    v.findViewById(R.id.wc_link).setOnClickListener(v1 -> linkWeChat());
    v.findViewById(R.id.wc_unlink).setOnClickListener(v1 -> unlinkWeChat());
    v.findViewById(R.id.wc_check).setOnClickListener(v1 -> checkWeChatAccountUsage());
    v.findViewById(R.id.delete_account).setOnClickListener(v1 -> showDeleteAccountDialog());

    return v;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    activeProfileFlowable =
        profileFacade
            .activeProfileFlowable()
            .doOnNext(
                newActiveProfile ->
                    Timber.e("New active profile is %s", newActiveProfile.getFirstName()))
            .share();

    reloadWeeklyStatOnActiveProfileChange();
    reloadBrushingsOnActiveProfileChange();
    initWeChatAndPhone();
  }

  // STATS

  /** Refresh weekly stat every time we change active profile */
  private void reloadWeeklyStatOnActiveProfileChange() {
    disposables.add(
        activeProfileFlowable
            .subscribeOn(Schedulers.io())
            .switchMap(profile -> dashboardCalculatorView.getWeeklyStatForProfile(profile.getId()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::updateStatGraphes, Throwable::printStackTrace // display error
                ));
  }

  /** Display some stats from the weeklyStat */
  private void updateStatGraphes(WeeklyStat weeklyStat) {
    tvAverageBrushingDuration.setText(
        String.format(Locale.US, "%d sec", weeklyStat.getAverageBrushingTime()));
    tvAverageBrushingSurface.setText(
        String.format(Locale.US, "%d %%", weeklyStat.getAverageSurface()));
  }

  /** Get total nb brushings for a profile */
  private void reloadBrushingsOnActiveProfileChange() {
    disposables.add(
        activeProfileFlowable
            .subscribeOn(Schedulers.io())
            .switchMap(profile -> brushingManager.brushingsFlowable(profile.getId()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                list -> updateNbBrushing(list.size()), Throwable::printStackTrace // display error
                ));
  }

  /** Display the total number of brushings on the screen */
  private void updateNbBrushing(long nb) {
    tvNumberBrushings.setText(String.format(Locale.US, "%d", nb));
  }

  private void initWeChatAndPhone() {
    final String phoneNumber = accountFacade.getAccount().getPhoneNumber();
    if (phoneNumber == null) {
      phoneNumberEdit.setText(R.string.unlinked);
    } else {
      phoneNumberEdit.setText(phoneNumber);
    }

    final WeChatData wcData = accountFacade.getAccount().getWeChatData();
    if (wcData == null) {
      wcCodeText.setText(R.string.unlinked);
    } else {
      wcCodeText.setText(wcData.getOpenId());
    }
  }

  /*
  Unlink a phone number
   */
  private void unlinkPhoneNumber() {
    disposables.add(
        accountFacade
            .unlinkPhoneNumber()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> phoneNumberEdit.setText(R.string.unlinked), this::onBackendError));
  }

  /*
  Send a verification sms to the number
   */
  private void sendSmsCode() {
    final String number = phoneNumberEdit.getText().toString();
    disposables.add(
        accountFacade
            .verifyPhoneNumber(number)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                link -> {
                  phoneNumberLink = link;
                  confirmationPanel.setVisibility(View.VISIBLE);
                },
                this::onBackendError));
  }

  /*
  Link the phone number using the verification code received by sms
   */
  private void linkPhoneNumber() {
    final int confirmationCode = Integer.parseInt(verificationCodeEdit.getText().toString());
    disposables.add(
        accountFacade
            .linkPhoneNumber(phoneNumberLink, confirmationCode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                  confirmationPanel.setVisibility(View.GONE);
                  verificationCodeEdit.setText("");
                },
                error -> {
                  confirmationPanel.setVisibility(View.GONE);
                  verificationCodeEdit.setText("");
                  onBackendError(error);
                }));
  }

  /*
  Link WeChat
   */

  private void linkWeChat() {
    WXApiManager.getInstance().setCurrentRequestCode(WXApiManager.REQUEST_CODE_LINK_WECHAT);
    ((MainActivity) getActivity()).onWeChatLogin();
  }

  private void unlinkWeChat() {
    disposables.add(
        accountFacade
            .unlinkWeChat()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                  wcCodeText.setText(R.string.unlinked);
                  Toast.makeText(getContext(), "Success!", Toast.LENGTH_LONG).show();
                },
                this::onBackendError));
  }

  /*
  Check WeChat account usage
   */

  private void checkWeChatAccountUsage() {
    WXApiManager.getInstance().setCurrentRequestCode(WXApiManager.REQUEST_CODE_CHECK_ACCOUNT_EXIST);
    ((MainActivity) getActivity()).onWeChatLogin();
  }

  private void showDeleteAccountDialog() {
    new AlertDialog.Builder(getContext())
        .setCancelable(true)
        .setMessage("Confirm to delete your account")
        .setPositiveButton("OK", (dialog, which) -> deleteAccount())
        .setNegativeButton("Cancel", null)
        .create()
        .show();
  }

  private void deleteAccount() {
    disposables.add(
        accountFacade
            .deleteAccount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::deleteSuccess, this::onBackendError));
  }

  private void deleteSuccess() {
    Toast.makeText(getContext(), "Your account has been deleted.", Toast.LENGTH_LONG).show();
    dataStore.clean();
    LoginActivity.start(getContext());
    getActivity().finish();
  }

  private void onBackendError(Throwable error) {
    if (error instanceof ApiError) {
      final ApiError linkError = (ApiError) error;
      Toast.makeText(getContext(), linkError.toString(), Toast.LENGTH_LONG).show();
    } else {
      error.printStackTrace();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(WeChatEvent event) {
    if (event.getId() != null) {
      wcCodeText.setText(event.getId());
    }
  }
}
