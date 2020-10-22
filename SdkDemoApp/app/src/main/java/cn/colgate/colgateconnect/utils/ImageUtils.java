package cn.colgate.colgateconnect.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import cn.colgate.colgateconnect.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

/** Some utils to manage images */
public class ImageUtils {

  private final Context context;
  private RequestOptions requestOptions = new RequestOptions();

  public ImageUtils(Context context) {
    this.context = context.getApplicationContext();
  }

  /**
   * Load the given image into the a view
   *
   * @param path path of the image to load
   * @param view view to load the image in
   */
  public void loadAvatar(String path, ImageView view) {
    Glide.with(context)
        .load(path != null ? path : R.drawable.boy)
        .transform(new CircleCrop())
        .into(view);
  }

  public String getImagePath(Uri uri) {
    Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
    cursor.moveToFirst();
    String document_id = cursor.getString(0);
    document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
    cursor.close();

    cursor =
        context
            .getContentResolver()
            .query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Images.Media._ID + " = ? ",
                new String[] {document_id},
                null);
    cursor.moveToFirst();
    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
    cursor.close();

    return path;
  }
}
