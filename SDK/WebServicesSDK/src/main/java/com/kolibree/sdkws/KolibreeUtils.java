package com.kolibree.sdkws;

import static com.kolibree.android.network.NetworkConstants.DEFAULT_HTTP_CONNECTION_TIMEOUT;
import static com.kolibree.android.network.NetworkConstants.DEFAULT_HTTP_READ_TIMEOUT;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.util.Base64;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kolibree.sdkws.networking.RequestMethod;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;

/** Created by aurelien on 15/09/15. */
@Keep
public class KolibreeUtils {

  public static final int KOLIBREE_START_OF_DAY_HOUR = 4;
  private static final String PREF_DEVICE_ID = "deviceid";
  private static final String DIGEST = "HmacSHA256";
  private final SharedPreferences prefs;

  @Inject
  public KolibreeUtils(@NonNull SharedPreferences prefs) {
    this.prefs = prefs;
  }

  /**
   * Encrypt with Kolibree app secret key
   *
   * @param in The String to be encrypted
   * @param key The app key
   * @return A Base64 encoded String
   * @throws NoSuchAlgorithmException if no one is found
   */
  public String encrypt(String in, String key)
      throws InvalidKeyException, NoSuchAlgorithmException {
    final Mac sha256_HMAC = Mac.getInstance(DIGEST);
    final SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), DIGEST);

    sha256_HMAC.init(secret_key);
    // Encryption + base64 encoding
    return Base64.encodeToString(sha256_HMAC.doFinal(in.getBytes()), Base64.DEFAULT);
  }

  /**
   * Upload a picture to uploadUrl
   *
   * @param uploadUrl the url to upload the picture to
   * @param file the bitmap picture file
   */
  public void uploadPicture(String uploadUrl, File file) throws IOException {
    HttpURLConnection httpCon = null;

    try {
      final URL connect = new URL(uploadUrl);
      httpCon = (HttpURLConnection) connect.openConnection();
      httpCon.setConnectTimeout((int) (DEFAULT_HTTP_CONNECTION_TIMEOUT.toMillis()));
      httpCon.setReadTimeout((int) (DEFAULT_HTTP_READ_TIMEOUT.toMillis()));
      httpCon.setRequestMethod(RequestMethod.PUT.name());
      httpCon.setRequestProperty("Content-Type", "image/jpeg");

      // Upload on the fly
      final FileInputStream in = new FileInputStream(file);
      IOUtils.copy(in, httpCon.getOutputStream());

      int responseCode;

      try {
        responseCode = httpCon.getResponseCode();
      } catch (IOException e) {
        e.printStackTrace();
        responseCode = 401;
      }
    } finally {
      if (httpCon != null) { // Properly close connection
        httpCon.disconnect();
      }
    }
  }

  /** Round a bitmap */
  @Nullable
  public Bitmap kolibrizeAvatar(Bitmap source) {
    if (source == null) {
      return null;
    }

    // Get the square centered part of the bitmap
    final int dim = Math.min(source.getWidth(), source.getHeight());
    final Bitmap square =
        ThumbnailUtils.extractThumbnail(source, dim, dim, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

    // Resize it to default avatar size
    final Bitmap bitmap =
        Bitmap.createScaledBitmap(
            square, Constants.AVATAR_SIZE_PX, Constants.AVATAR_SIZE_PX, false);
    square.recycle();
    return bitmap;
  }

  /**
   * Get the device unique ID
   *
   * @return String, The stored, or a new device ID
   */
  public String getDeviceId() {
    String deviceId = prefs.getString(PREF_DEVICE_ID, null);

    // If no one is stored, generate a new one
    if (deviceId == null) {
      deviceId = UUID.randomUUID().toString();
      prefs.edit().putString(PREF_DEVICE_ID, deviceId).apply();
    }

    return deviceId;
  }
}
