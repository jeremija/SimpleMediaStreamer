package hr.jsteiner.simplemediastreamer.lastfm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LastfmInfoMediator extends Activity {
  
  public static final String TAG = LastfmInfoMediator.class.getCanonicalName();
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    handleIntent(getIntent());
  }
  
  private void handleIntent(Intent intent) {
    if (intent == null) {
      Log.e(TAG + "#handleIntent(Intent)", "Intent is null!");
      return;
    }
    
    String action = intent.getAction();
    
    if (Intent.ACTION_VIEW.equals(action)) {
      
      if (intent.getData() != null) {
        String uri = intent.getData().toString();
        Log.d(TAG + "#handleIntent(Intent)", "Intent data=" + uri);
        InfoSearchParam isp = InfoSearchParam.fromLastfmUrl(uri);
        isp.startActivity(this);
      }
      else {
        Log.e(TAG + "#handleIntent(intent)", "No data (uri) for intent");
        finish();
      }
    }
  }
  
  
}
