package cn.colgate.colgateconnect.utils;

import android.util.Log;
import java.io.IOException;
import retrofit2.HttpException;

/** Created by Guillaume Agis on 16/11/2018. */

/** Display the body of error message */
public class ApiError {

  // get the body of the error
  public static void displayErrorMessage(Throwable t) {
    t.printStackTrace();
    // to get the error body from a request
    if (t instanceof HttpException) {
      HttpException error = (HttpException) t;
      String errorBody = null;
      try {
        errorBody = error.response().errorBody().string();
        Log.e("displayErrorMessage", "error body  : " + errorBody);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
