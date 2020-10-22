package cn.colgate.colgateconnect.register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import butterknife.OnClick;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.base.SdkDemoBaseActivity;
import cn.colgate.colgateconnect.home.MainActivity;

/** Display confirmation page when the account has been created */
public class ProfileCreatedActivity extends SdkDemoBaseActivity {

  public static void start(Context context) {
    Intent starter = new Intent(context, ProfileCreatedActivity.class);
    context.startActivity(starter);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile_created);
  }

  @OnClick(R.id.profile_created_ok)
  void onOkClicked() {
    MainActivity.start(this);
  }
}
