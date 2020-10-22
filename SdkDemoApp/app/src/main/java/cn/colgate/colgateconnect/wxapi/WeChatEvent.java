package cn.colgate.colgateconnect.wxapi;

public class WeChatEvent {
  private String id;

  WeChatEvent(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
