package hr.jsteiner.simplemediastreamer.async;

import hr.jsteiner.simplemediastreamer.ApplicationEx;
import hr.jsteiner.simplemediastreamer.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Extended {@link AsyncTaskCallback} class for downloading the images from internet. 
 * The parameters of {@link #execute(String...)} are passed to {@link #doInBackground(String...)}
 * and that method expects the String[] to have only URLs of images to download.   
 * @author jere
 *
 */
public class DownloadImagesTask extends AsyncTaskCallback<String, Integer, List<Bitmap>> {
  
  public static final String TAG = DownloadImagesTask.class.getCanonicalName();
  
  public static ApplicationEx mAppContext = null;
  
  public static void setAppContext(ApplicationEx appContext) {
    mAppContext = appContext;
  }

  public int CONNECT_TIMEOUT = 0;
  public int READ_TIMEOUT = 0;
  
  @Override
  protected List<Bitmap> doInBackground(String... params) {
    if (params.length < 1 ) {
      throw new IllegalArgumentException("only one parameter accepted");
    }
    
    List<Bitmap> bitmaps = new ArrayList<Bitmap>();
    
    for (String imageLocation : params) {
    
      Bitmap bitmap = null;
      
      try {
        URL url = new URL(imageLocation);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setDoInput(true);
        connection.connect();
        InputStream is = connection.getInputStream();
        bitmap = BitmapFactory.decodeStream(is);
        Log.v(TAG, "bitmap fetched");
      }
      catch (MalformedURLException e) {
        mAppContext.getConsole().log(R.string.error_downloading_image + " " + imageLocation);
      }
      catch (IOException e) {
        mAppContext.getConsole().log(e.toString());
      }
      
      bitmaps.add(bitmap);
    
    }
    
    return bitmaps;
  }

  
}
