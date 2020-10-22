package cn.colgate.colgateconnect.wxapi;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXApiManager {
  public static final int REQUEST_CODE_LOGIN_WECHAT = 0;
  public static final int REQUEST_CODE_LINK_WECHAT = 1;
  public static final int REQUEST_CODE_CHECK_ACCOUNT_EXIST = 2;
  private static final WXApiManager INSTANCE = new WXApiManager();
  private static final String WE_CHAT_APP_ID = "wx8cdcdd2e4c0a6823";
  private int currentRequestCode;
  private IWXAPI api;

  private WXApiManager() {}

  public static WXApiManager getInstance() {
    return INSTANCE;
  }

  private IWXAPI getApi(Context context) {
    return api != null ? api : WXAPIFactory.createWXAPI(context, WE_CHAT_APP_ID, true);
  }

  public void setupWXApi(Context context) {
    api = getApi(context);
    api.registerApp(WE_CHAT_APP_ID);
  }

  boolean handleIntent(Intent intent, IWXAPIEventHandler handler) {
    return api.handleIntent(intent, handler);
  }

  public void requestWeChatCode(Context context) {
    if (api != null && api.isWXAppInstalled()) {
      final SendAuth.Req req = new SendAuth.Req();
      req.scope = "snsapi_userinfo";
      req.state = "wechat_sdk_demo_test";
      api.sendReq(req);
    } else {
      Toast.makeText(context, "WeChat is not installed", Toast.LENGTH_SHORT).show();
    }
  }

  int getCurrentRequestCode() {
    return currentRequestCode;
  }

  public void setCurrentRequestCode(int currentRequestCode) {
    this.currentRequestCode = currentRequestCode;
  }
}
