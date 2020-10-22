package com.kolibree.sdkws.data.model.gopirate;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.sdkws.data.JSONModel;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Created by aurelien on 16/11/15. */
@Keep
public class UpdateGoPirateData implements JSONModel {
  private static final String FIELD_RANK = "rank";
  private static final String FIELD_GOLD = "gold";
  private static final String FIELD_LAST_WORLD_REACHED = "last_world_reached";
  private static final String FIELD_LAST_LEVEL_REACHED = "last_level_reached";
  private static final String FIELD_LAST_LEVEL_BRUSH = "last_level_brush";
  private static final String FIELD_LAST_SHIP_BOUGHT = "last_ship_bought";
  private static final String FIELD_AVATAR_COLOR = "avatar_color";
  private static final String FIELD_NEW_TREASURES = "new_treasures";
  private static final String FIELD_BRUSHING_COUNT = "brushing_count";

  private int rank;
  private int gold;
  private int lastWorldReached;
  private int lastLevelReached;
  private int lastLevelBrush;
  private int lastShipBought;
  private int avatarColor;
  private ArrayList<Integer> newTreasures;
  private int brushing;

  public UpdateGoPirateData() {
    this.rank = -1;
    this.gold = -1;
    this.lastWorldReached = -1;
    this.lastLevelReached = -1;
    this.lastLevelBrush = -1;
    this.lastShipBought = -1;
    this.avatarColor = -1;
    this.newTreasures = new ArrayList<>();
    this.brushing = -1;
  }

  public UpdateGoPirateData(String raw) throws JSONException {
    final JSONObject json = new JSONObject(raw);
    rank = json.getInt(FIELD_RANK);
    gold = json.getInt(FIELD_GOLD);
    lastWorldReached = json.getInt(FIELD_LAST_WORLD_REACHED);
    lastLevelReached = json.getInt(FIELD_LAST_LEVEL_REACHED);
    lastLevelBrush = json.getInt(FIELD_LAST_LEVEL_BRUSH);
    lastShipBought = json.getInt(FIELD_LAST_SHIP_BOUGHT);
    avatarColor = json.getInt(FIELD_AVATAR_COLOR);
    brushing = json.getInt(FIELD_BRUSHING_COUNT);

    final JSONArray array = json.getJSONArray(FIELD_NEW_TREASURES);
    newTreasures = new ArrayList<>();

    for (int i = 0; i < array.length(); i++) {
      newTreasures.add(array.getInt(i));
    }
  }

  public void onGoldEarned(int amount) {
    this.gold += amount;
  }

  public void onNewTreasure(int newTreasure) {
    this.newTreasures.add(newTreasure);
  }

  public void onBrushing() {
    brushing = 1;
  }

  public boolean hasBrushing() {
    return brushing == 1;
  }

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public int getGold() {
    return gold;
  }

  public int getLastWorldReached() {
    return lastWorldReached;
  }

  public void setLastWorldReached(int lastWorldReached) {
    this.lastWorldReached = lastWorldReached;
  }

  public int getLastLevelReached() {
    return lastLevelReached;
  }

  public void setLastLevelReached(int lastLevelReached) {
    this.lastLevelReached = lastLevelReached;
  }

  public int getLastLevelBrush() {
    return lastLevelBrush;
  }

  public void setLastLevelBrush(int lastLevelBrush) {
    this.lastLevelBrush = lastLevelBrush;
  }

  public int getLastShipBought() {
    return lastShipBought;
  }

  public void setLastShipBought(int lastShipBought) {
    this.lastShipBought = lastShipBought;
  }

  public int getAvatarColor() {
    return avatarColor;
  }

  public void setAvatarColor(int avatarColor) {
    this.avatarColor = avatarColor;
  }

  public ArrayList<Integer> getNewTreasures() {
    return newTreasures;
  }

  public boolean hasTreasure(int id) {
    return newTreasures.contains(id);
  }

  public int getBrushing() {
    return brushing;
  }

  @NonNull
  @Override
  public String toJsonString() throws JSONException {
    final JSONObject json = new JSONObject();

    if (rank != -1) json.put(FIELD_RANK, rank);
    if (gold != -1) json.put(FIELD_GOLD, gold);
    if (lastWorldReached != -1) json.put(FIELD_LAST_WORLD_REACHED, lastWorldReached);
    if (lastLevelReached != -1) json.put(FIELD_LAST_LEVEL_REACHED, lastLevelReached);
    if (lastShipBought != -1) json.put(FIELD_LAST_SHIP_BOUGHT, lastShipBought);
    if (lastLevelBrush != -1) json.put(FIELD_LAST_LEVEL_BRUSH, lastLevelBrush);
    if (avatarColor != -1) json.put(FIELD_AVATAR_COLOR, avatarColor);

    if (newTreasures.size() > 0) {
      final JSONArray treasures = new JSONArray();

      for (Integer treasure : newTreasures) {
        treasures.put(treasure);
      }

      json.put(FIELD_NEW_TREASURES, treasures);
    }

    return json.toString();
  }

  @Override
  public String toString() {
    final JSONObject json = new JSONObject();

    try {
      json.put(FIELD_RANK, rank);
      json.put(FIELD_GOLD, gold);
      json.put(FIELD_LAST_WORLD_REACHED, lastWorldReached);
      json.put(FIELD_LAST_LEVEL_REACHED, lastLevelReached);
      json.put(FIELD_LAST_SHIP_BOUGHT, lastShipBought);
      json.put(FIELD_LAST_LEVEL_BRUSH, lastLevelBrush);
      json.put(FIELD_AVATAR_COLOR, avatarColor);
      json.put(FIELD_BRUSHING_COUNT, brushing);

      final JSONArray treasures = new JSONArray();

      for (Integer treasure : newTreasures) {
        treasures.put(treasure);
      }

      json.put(FIELD_NEW_TREASURES, treasures);
    } catch (JSONException e) {
    }

    return json.toString();
  }
}
