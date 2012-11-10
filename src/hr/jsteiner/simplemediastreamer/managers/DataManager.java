package hr.jsteiner.simplemediastreamer.managers;

import hr.jsteiner.simplemediastreamer.ApplicationEx;
import hr.jsteiner.simplemediastreamer.domain.LastfmUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class DataManager {
  public final static String TAG = DataManager.class.getCanonicalName();
  
  public final static String LASTFM_SESSION = "LASTFM_SESSION";
  public static final int MODE_PRIVATE = 0;
  
  public static ApplicationEx mAppContext = null;
  
  public static void setAppContext(ApplicationEx appContext) {
    mAppContext = appContext;
  }
  
  public static String getSessionKey() {
    LastfmUser user = getCurrentLastfmUser();
    if (user == null) {
      return null;
    }
    return user.getSessionKey();
  }
  
  /**
   * Get the user from shared preferences. Usually called from an Activity.
   * @param context use getApplicationContext() if calling from an Activity.
   * @return
   */
  public static LastfmUser getCurrentLastfmUser() {
    SharedPreferences settings = mAppContext.getSharedPreferences(LASTFM_SESSION, MODE_PRIVATE);
    String session = settings.getString("session", null);
    String username = settings.getString("username", null);
    boolean subscriber = settings.getBoolean("subscriber", false);
    
    if (session == null || username == null) {
      Log.d(TAG + "#getCurrentLastFmUser(Context)", "No user found, will return null");
      return null;
    }
    
    LastfmUser user = new LastfmUser(username, session, subscriber);
    Log.d(TAG + "#getCurrentLastfmUser(Context)", 
        "current lastfm user loaded: " + (user != null ? user.toString() : "(null)"));
    return user;
  }
  
  /**
   * Save the user to shared preferences. Usually called from an Activity.
   * @param context use getApplicationContext() if calling from an Activity.
   * @param user to save
   */
  public static void saveCurrentLastfmUser(LastfmUser user) {
    String username = null;
    String session = null;
    boolean subscriber = false;
    
    if (user != null) {
      username = user.getUsername();
      session = user.getSessionKey();
      subscriber = user.isSubscriber();
    }
    
    SharedPreferences settings = mAppContext.getSharedPreferences(LASTFM_SESSION, MODE_PRIVATE);
    Editor settingsEditor = settings.edit();
    
    settingsEditor.putString("username", username);
    settingsEditor.putString("session", session);
    settingsEditor.putBoolean("subscriber", subscriber);
    
    settingsEditor.commit();
    
    Log.d(TAG + "#saveCurrentLastfmUser(Context, LastfmUser)", 
        "current lastfm user saved: " + (user != null ? user.toString() : "(null)"));
  }
  
  public static void addStationToPreviousStationsList(String stationUrl) {
    SharedPreferences settings = mAppContext.getSharedPreferences(LASTFM_SESSION, MODE_PRIVATE);
    if (stationUrl == null) {
      return;
    }
    
    List<String> previousStations = 
        new ArrayList<String>(Arrays.asList(loadPreviousStationsList()));
    
    Log.v(TAG + "#addStationToPreviousStations(Context,String)", 
        "old previousStations=" + previousStations.toString());
    
    if (previousStations.contains(stationUrl)) {
        previousStations.remove(previousStations.indexOf(stationUrl));
    }
    else {
      while (previousStations.size() > 4) {
        previousStations.remove(4);
      }
    }
    
    previousStations.add(0, stationUrl);
    
    Log.v(TAG + "#addStationToPreviousStations(Context,String)", 
        "new previousStations=" + previousStations.toString());
    
    Editor settingsEditor = settings.edit();
    for (int index =0; index < previousStations.size(); index ++ ) {
      String previousStation = previousStations.get(index);
      if (previousStation != null) {
        Log.v(TAG + "#addStationToPreviousStationsList(Context,String)", 
          "saving station url index: " + index + ", statonUrl=" + stationUrl);
        settingsEditor.putString("previousStationUrl" + (index+1), previousStation);
      }
    }
    
    settingsEditor.commit();
  }
  
  public static String[] loadPreviousStationsList() {
    SharedPreferences settings = mAppContext.getSharedPreferences(LASTFM_SESSION, MODE_PRIVATE);
    
    String previousStationUrl1 = settings.getString("previousStationUrl1", null);
    String previousStationUrl2 = settings.getString("previousStationUrl2", null);
    String previousStationUrl3 = settings.getString("previousStationUrl3", null);
    String previousStationUrl4 = settings.getString("previousStationUrl4", null);
    String previousStationUrl5 = settings.getString("previousStationUrl5", null);
    
    List<String> previousStations = new ArrayList<String>();
    if (previousStationUrl1 != null) previousStations.add(previousStationUrl1);
    if (previousStationUrl2 != null) previousStations.add(previousStationUrl2);
    if (previousStationUrl3 != null) previousStations.add(previousStationUrl3);
    if (previousStationUrl4 != null) previousStations.add(previousStationUrl4);
    if (previousStationUrl5 != null) previousStations.add(previousStationUrl5);
    
    return previousStations.toArray(new String[previousStations.size()]);
  }
  
  public static String loadLastTunedStationUrl(Context context) {
    
    SharedPreferences settings = mAppContext.getSharedPreferences(LASTFM_SESSION, MODE_PRIVATE);
    
    String stationUrl = settings.getString("previousStationUrl1", "");
    Log.d(TAG + "#loadLastTunedStationUrl(Context)", "stationUrl=" + stationUrl);
    
    return stationUrl;
  }
  
  public static void saveLastTunedStationUrl(Context context, String stationUrl) {
    if (stationUrl == null || "".equals(stationUrl)) {
      return;
    }
    
    addStationToPreviousStationsList(stationUrl);
    
    Log.d(TAG + "#saveLastTunedStationUrl(Context,String)", "stationUrl=" + stationUrl);
  }
  
  public static boolean isLowBitrate() {
    SharedPreferences settings = mAppContext.getSharedPreferences(LASTFM_SESSION, MODE_PRIVATE);
    
    boolean lowBitrate = settings.getBoolean("lowBitrate", false);
    
    return lowBitrate;
  }
  
  public static void setLowBitrate(boolean lowBitrate) {
    SharedPreferences settings = mAppContext.getSharedPreferences(LASTFM_SESSION, MODE_PRIVATE);
    
    Editor settingsEditor = settings.edit();
    settingsEditor.putBoolean("lowBitrate", lowBitrate);
    
    settingsEditor.commit();
  }
}
