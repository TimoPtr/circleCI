package cn.colgate.colgateconnect.register;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.auth.AuthenticationFlowActivity;
import cn.colgate.colgateconnect.auth.result.AuthenticationResultData;
import cn.colgate.colgateconnect.base.SdkDemoBaseActivity;
import cn.colgate.colgateconnect.demo.AccountInfo;
import com.kolibree.android.accountinternal.profile.models.IProfile;
import com.kolibree.android.commons.profile.Gender;
import com.kolibree.android.commons.profile.Handedness;
import com.kolibree.sdkws.sms.data.AccountData;
import dagger.android.AndroidInjection;
import java.util.List;
import javax.inject.Inject;
import org.threeten.bp.LocalDate;

/** Register new account using the phone number validation. */
public class RegisterActivity extends SdkDemoBaseActivity {

  private static final int REGISTER_FLOW = 101;

  @Inject AccountInfo accountInfo;

  @BindView(R.id.profile_info_name)
  EditText userNameEditText;

  @BindView(R.id.profile_info_right_hand)
  ImageView rightHand;

  @BindView(R.id.profile_info_left_hand)
  ImageView leftHand;

  @BindView(R.id.profile_info_male)
  ImageView male;

  @BindView(R.id.profile_info_female)
  ImageView female;

  private boolean isRightHanded;
  private boolean isMale;

  public static void start(Context context) {
    Intent starter = new Intent(context, RegisterActivity.class);
    context.startActivity(starter);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile_settings);
  }

  // create a new account and call the SMS auth module with a view to go forward
  // Please refer to the documentation if you with to use the SMS auth without the UI
  @OnClick(R.id.profile_info_next)
  void createAccount() {

    AccountData accountData =
        new AccountData(
            LocalDate.of(1990, 12, 12),
            getGender(),
            getHandedness(),
            "CH",
            userNameEditText.getText().toString());
    Intent intent = AuthenticationFlowActivity.createAccountFlow(this, accountData);
    startActivityForResult(intent, REGISTER_FLOW);
  }

  // Account associated to the current user
  private void accountCreated(IProfile profile) {
    accountInfo.setCurrentProfile(profile);
    ProfileCreatedActivity.start(this);
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == REGISTER_FLOW) {
      if (resultCode == Activity.RESULT_OK) {
        if (AuthenticationFlowActivity.wasSuccess(resultCode)) {
          AuthenticationResultData result = AuthenticationFlowActivity.extractResultData(data);
          if (result != null) {
            List<IProfile> profiles = result.getProfiles();
            if (!profiles.isEmpty()) {
              accountCreated(profiles.get(0));
            } else {
              Toast.makeText(this, "No profile associated to this account", Toast.LENGTH_LONG)
                  .show();
            }
          } else {
            Toast.makeText(this, "No data received !", Toast.LENGTH_LONG).show();
          }
        } else {
          Toast.makeText(this, "Account Not created", Toast.LENGTH_LONG).show();
        }
      }
    }
  } // onActivityResult

  @OnClick(R.id.profile_info_left_hand)
  void onLeftHandClick() {
    hideKeyboard();

    renderHandedness(Handedness.LEFT_HANDED);
  }

  @OnClick(R.id.profile_info_right_hand)
  void onRightHandClick() {
    hideKeyboard();

    renderHandedness(Handedness.RIGHT_HANDED);
  }

  @OnClick(R.id.profile_info_male)
  void onMaleClick() {
    hideKeyboard();

    renderGender(Gender.MALE);
  }

  @OnClick(R.id.profile_info_female)
  void onFemaleClick() {
    hideKeyboard();

    renderGender(Gender.FEMALE);
  }

  private Gender getGender() {
    return isMale ? Gender.MALE : Gender.FEMALE;
  }

  private Handedness getHandedness() {
    return isRightHanded ? Handedness.RIGHT_HANDED : Handedness.LEFT_HANDED;
  }

  private void renderGender(Gender gender) {
    if (gender != null) {
      isMale = gender == Gender.MALE;
      if (gender == Gender.MALE) {
        male.setImageResource(R.drawable.btn_male_selected);
        female.setImageResource(R.drawable.btn_female);
      } else {
        male.setImageResource(R.drawable.btn_male);
        female.setImageResource(R.drawable.btn_female_selected);
      }
    }
  }

  private void renderHandedness(Handedness handedness) {
    if (handedness != null) {
      isRightHanded = handedness == Handedness.RIGHT_HANDED;
      if (handedness == Handedness.LEFT_HANDED) {
        leftHand.setImageResource(R.drawable.btn_left_hand_selected);
        rightHand.setImageResource(R.drawable.btn_right_hand);
      } else {
        leftHand.setImageResource(R.drawable.btn_left_hand);
        rightHand.setImageResource(R.drawable.btn_right_hand_selected);
      }
    }
  }

  private void hideKeyboard() {
    if (userNameEditText.getWindowToken() != null) {
      ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
          .hideSoftInputFromWindow(userNameEditText.getWindowToken(), 0);

      userNameEditText.clearFocus();
    }
  }
}
