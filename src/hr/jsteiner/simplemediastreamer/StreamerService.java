package hr.jsteiner.simplemediastreamer;

import hr.jsteiner.simplemediastreamer.async.AsyncTaskCallback.OnTaskFinishListener;
import hr.jsteiner.simplemediastreamer.async.AsyncTaskCallbackWrapper;
import hr.jsteiner.simplemediastreamer.async.AsyncTaskCallbackWrapper.BackgroundTask;
import hr.jsteiner.simplemediastreamer.domain.LastfmUser;
import hr.jsteiner.simplemediastreamer.domain.Playlist;
import hr.jsteiner.simplemediastreamer.domain.RadioTrack;
import hr.jsteiner.simplemediastreamer.domain.Station;
import hr.jsteiner.simplemediastreamer.managers.DataManager;
import hr.jsteiner.simplemediastreamer.managers.LastfmManager;
import hr.jsteiner.simplemediastreamer.managers.LastfmManager.LoveTrackAction;
import hr.jsteiner.simplemediastreamer.media.Player;
import hr.jsteiner.simplemediastreamer.media.Player.OnBufferingUpdateListener;
import hr.jsteiner.simplemediastreamer.media.Player.OnHalfTrackPassedListener;
import hr.jsteiner.simplemediastreamer.media.Player.OnPlaybackStateChangedListener;
import hr.jsteiner.simplemediastreamer.media.Player.OnProgressUpdateListener;
import hr.jsteiner.simplemediastreamer.media.Player.OnRequestPlaylistUpdateListener;
import hr.jsteiner.simplemediastreamer.media.Player.OnTrackChangedListener;
import hr.jsteiner.simplemediastreamer.media.Player.PlayerState;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class StreamerService extends Service implements 
    OnProgressUpdateListener, OnBufferingUpdateListener, OnPlaybackStateChangedListener, 
    OnTrackChangedListener, OnRequestPlaylistUpdateListener, OnHalfTrackPassedListener
{
  public static final String ACTION_TUNE_IN = "ACTION_TUNE_IN";
  public static final String ACTION_PLAY = "ACTION_PLAY";
  public static final String ACTION_PAUSE = "ACTION_PAUSE";
  public static final String ACTION_STOP = "ACTION_STOP";
  public static final String ACTION_NEXT = "ACTION_NEXT";
  public static final String ACTION_NEXT_WITH_INDEX = "ACTION_NEXT_WITH_INDEX";
  public static final String ACTION_LOVE_TRACK = "ACTION_LOVE_TRACK";
  public static final String ACTION_GET_PLAYLIST = "ACTION_GET_PLAYLIST";
  public static final String ACTION_GET_CURRENT_TRACK = "ACTION_GET_CURRENT_TRACK";
  
  public static final String BROADCAST_BUFFER_UPDATE = "BROADCAST_BUFFER_UPDATE";
  public static final String BROADCAST_TRACK_PROGRESS = "BROADCAST_TRACK_PROGRESS";
  public static final String BROADCAST_TRACK = "BROADCAST_TRACK";
  public static final String BROADCAST_PLAYLIST = "BROADCAST_PLAYLIST";
  
  private final Intent mBroadcastTrackProgressIntent = new Intent(BROADCAST_TRACK_PROGRESS);
  private final Intent mBroadcastBufferUpdateIntent = new Intent(BROADCAST_BUFFER_UPDATE);
  
//  private static final String SAMPLE_STREAM_URL = "http://www.robtowns.com/music/blind_willie.mp3";
  private static final int ONGOING_NOTIFICATION = 234152545;
  private static final String TAG = StreamerService.class.getCanonicalName();
    
  private final Player mPlayer = new Player();
  
  private TelephonyManager mTelephonyManager;
  
  public ApplicationEx getAppContext() {
    return (ApplicationEx) getApplicationContext();
  }
  
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    String action = null;
    if (intent != null) {
      action = intent.getAction();
    }
    
    if (ACTION_TUNE_IN.equals(action)) {
      String stationUrl = intent.getStringExtra("stationUrl");
      Log.d(TAG + "#onStartCommand(Intent, int, int)", "Tuning to stationUrl=" + stationUrl);
      lastFmTuneIn(stationUrl);
    }
    
    if (ACTION_PLAY.equals(action)) {
      //TODO play not yet implemented (and probably won't be)
    }
    
    if (ACTION_STOP.equals(action)) {
      int result = mPlayer.stop();
      if (result != 0) getAppContext().getConsole().toast(getString(result));
    }
    
    if (ACTION_PAUSE.equals(action)) {
      int result = mPlayer.pause();
      if (result != 0) getAppContext().getConsole().toast(getString(result));
    }
    
    if (ACTION_NEXT.equals(action)) {
      new Thread(new Runnable() {
        @Override public void run() {
          final int result = mPlayer.nextInPlaylist();
          if (result != 0) getAppContext().getConsole().toast(getString(result));
        }
      }).start();
    }
    
    if (ACTION_NEXT_WITH_INDEX.equals(action)) {
      int trackIndex = intent.getIntExtra("trackIndex", -1);
      int result = mPlayer.play(trackIndex);
      if (result != 0) getAppContext().getConsole().toast(getString(result));
    }
    
    if (ACTION_LOVE_TRACK.equals(action)) {
      String loveAction = intent.getStringExtra("loveAction");
      
      RadioTrack track = mPlayer.getCurrentTrack();
      LoveTrackAction loveActionEnum = null;
      if ("love".equals(loveAction)) loveActionEnum = LoveTrackAction.LOVE;
      else if ("unlove".equals(loveAction)) loveActionEnum = LoveTrackAction.UNLOVE;
      else if ("ban".equals(loveAction)) loveActionEnum = LoveTrackAction.BAN;
      else if ("unban".equals(loveAction)) loveActionEnum = LoveTrackAction.UNBAN;
      
      loveTrack(track, loveActionEnum);
    }
    
    if (ACTION_GET_PLAYLIST.equals(action)) {
      Intent playlistBroadcastIntent = new Intent(BROADCAST_PLAYLIST);
      playlistBroadcastIntent.putExtra("playlist", mPlayer.getPlaylist());
      playlistBroadcastIntent.putExtra("currentTrackIndex", mPlayer.getCurrentTrackIndex());
      sendBroadcast(playlistBroadcastIntent);
    }
    
    if (ACTION_GET_CURRENT_TRACK.equals(action)) {
      Intent trackBroadcastIntent = new Intent(BROADCAST_TRACK);
      trackBroadcastIntent.putExtra("track", mPlayer.getCurrentTrack());
      sendBroadcast(trackBroadcastIntent);
    }
    
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onCreate() {
    getAppContext().getConsole().log("StreamerService started");
    
    mPlayer.setOnBufferingUpdateListener(this);
    mPlayer.setOnProgressUpdateListener(this);
    mPlayer.setOnTrackChangedListener(this);
    mPlayer.setOnRequestPlaylistUpdateListener(this);
    mPlayer.setOnPlaybackStateChangedListener(this);
    mPlayer.setOnHalfTrackPassedListener(this);
    
    mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    mTelephonyManager.listen(new PhoneStateListenerExtended(), PhoneStateListener.LISTEN_CALL_STATE);
    
  }
  
  @Override
  public void onDestroy() {
//    stopForeground(true);
    mPlayer.stop();
    
    getAppContext().getConsole().log("StreamerService destroyed");
    super.onDestroy();
  }
  
  
  
  public Player getPlayer() {
    return mPlayer;
  }

  @Override
  @SuppressWarnings("deprecation")
  public void onPlaybackStateChanged(PlayerState state, RadioTrack track, int duration, int currentTrackIndex) {
    String trackTitle = "";
    String trackArtist = "";
    String trackArtistWithBrackets = "";
    if (track != null) {
      trackTitle = track.getTitle();
      trackArtist = track.getCreator();
      trackArtistWithBrackets = " (" + trackArtist + ")";
    }
    if (PlayerState.PLAYING.equals(state)) {
      Notification note=new Notification(android.R.drawable.ic_media_play,
          trackTitle + trackArtistWithBrackets,
          System.currentTimeMillis());
      Intent intent = new Intent(this, StreamerActivity.class);

      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

      PendingIntent pi=PendingIntent.getActivity(this, 0, intent, 0);
      note.setLatestEventInfo(this, trackTitle, trackArtist, pi);
      note.flags|=Notification.FLAG_NO_CLEAR;
      
      startForeground(ONGOING_NOTIFICATION, note);
    }
    if (PlayerState.STOPPED.equals(state)) {
      stopForeground(true);
      onProgressUpdate(0, 0, 0);
    }
    if (PlayerState.PAUSED.equals(state)) {
      //stopForeground(true);
    }
  }
  
  @Override
  public void onTrackChanged(final RadioTrack track, int trackIndex) {
    Log.d(TAG + "#onTrackChanged(Track,int)", "track=" + track);
    getAppContext().getConsole().log("Track changed: " + track.getTitle());
    
    Intent trackBroadcastIntent = new Intent(BROADCAST_TRACK);
    trackBroadcastIntent.putExtra("track", track);
    sendBroadcast(trackBroadcastIntent);
    
    if (track != null) {
        Log.d(TAG + "#onTrackChanged(Track,int)", "Starting NowPlayingNotifyTask...");
        getAppContext().getConsole().log("Starting track: " + track.getLocation());
        AsyncTaskCallbackWrapper<Boolean> task = new AsyncTaskCallbackWrapper<Boolean>(
            new BackgroundTask<Boolean>() {
              @Override public Boolean doBackgroundTask() {
                LastfmManager.trackUpdateNowPlaying(track);
                return null;
              }});
        task.execute();
    }
  }
  
  @Override
  public void onProgressUpdate(int time, int percent, int duration) {
    mBroadcastTrackProgressIntent.putExtra("time", time);
    mBroadcastTrackProgressIntent.putExtra("percent", percent);
    mBroadcastTrackProgressIntent.putExtra("duration", duration);
    sendBroadcast(mBroadcastTrackProgressIntent);
  }
  
  @Override
  public void onBufferingUpdate(int percent) {
    mBroadcastBufferUpdateIntent.putExtra("percent", percent);
    sendBroadcast(mBroadcastBufferUpdateIntent);
  }
  
  @Override
  public void onRequestPlaylistUpdate() {
//    UpdatePlaylistTask task = new UpdatePlaylistTask();
//    task.execute();
    final String bitrate = DataManager.isLowBitrate() == true ? "64" : "128";
    
    getAppContext().getConsole().log("Requesting playlist update. Bitrate=" + bitrate);
    
    AsyncTaskCallbackWrapper<Playlist> updatePlaylistTask = new AsyncTaskCallbackWrapper<Playlist>(
        new BackgroundTask<Playlist>() {
          @Override public Playlist doBackgroundTask() {
            return LastfmManager.fetchPlaylist(bitrate);
          }});
    
    updatePlaylistTask.setOnTaskFinishListener(new OnTaskFinishListener<Playlist>() {
          @Override public void onTaskFinish(Playlist playlist) {
            getPlayer().updatePlaylist(playlist);
          }});
    
    updatePlaylistTask.execute();
  }
  
  @Override
  public void onHalfTrackPassed(final RadioTrack track) {
    if (track != null) {
      AsyncTaskCallbackWrapper<Boolean> scrobbleTask = new AsyncTaskCallbackWrapper<Boolean>(
          new BackgroundTask<Boolean>() {
            @Override public Boolean doBackgroundTask() {
              LastfmManager.trackScrobble(track);
              return null;
            }});
      scrobbleTask.setOnTaskFinishListener(new OnTaskFinishListener<Boolean>() {
        @Override public void onTaskFinish(Boolean result) {
          String resultMessage;
          if (Boolean.FALSE.equals(result)) {
            resultMessage = getString(R.string.scrobble_error);
          }
          else {
            resultMessage = getString(R.string.scrobble_success); 
          }
          getAppContext().getConsole().toast(resultMessage);
        }});
      scrobbleTask.execute();
    }
  }
  
  public void lastFmTuneIn(final String stationUrl) {
    getAppContext().getConsole().log("Trying to tune in to " + stationUrl);
    
    AsyncTaskCallbackWrapper<Station> tuneInTask = new AsyncTaskCallbackWrapper<Station>(
        new BackgroundTask<Station>() {
          @Override public Station doBackgroundTask() {
            return LastfmManager.tuneIn(stationUrl);
          }});
    
    tuneInTask.setOnTaskFinishListener(new OnTaskFinishListener<Station>() {
      @Override public void onTaskFinish(final Station station) {
        if (station == null) {
          getAppContext().getConsole().toast(getString(R.string.station_tune_failed));
          return;
        }
        else {
          getAppContext().getConsole().toast(getString(R.string.station_tune_success));
          DataManager.saveLastTunedStationUrl(getApplicationContext(), station.getLastfmUrl());
          getAppContext().setLastTunedStation(station);
        }
        
        final String bitrate = DataManager.isLowBitrate() == true ? "64" : "128";
        getAppContext().getConsole().log("Will try to fetch first playlist. Bitrate=" + bitrate);
        
        /* set playlist on successful tune in */
        AsyncTaskCallbackWrapper<Playlist> getPlaylistTask = new AsyncTaskCallbackWrapper<Playlist>(
            new BackgroundTask<Playlist>() {
              @Override public Playlist doBackgroundTask() {
                return LastfmManager.fetchPlaylist(bitrate);
              }});
        
        getPlaylistTask.setOnTaskFinishListener(new OnTaskFinishListener<Playlist>() {
              @Override public void onTaskFinish(Playlist playlist) {
                getPlayer().setPlaylist(playlist);
              }});
        
        getPlaylistTask.execute();
      }});
    
    tuneInTask.execute();
  }
  
  private void loveTrack(final RadioTrack track, final LoveTrackAction action) {
    if (track == null || action == null) {
      return;
    }
    
    LastfmUser user = DataManager.getCurrentLastfmUser();
    if (user == null) {
      getAppContext().getConsole().toast(getString(R.string.error_not_logged_in));
    }
    
    AsyncTaskCallbackWrapper<Integer> loveTrack = new AsyncTaskCallbackWrapper<Integer>(
        new BackgroundTask<Integer>() {
          @Override public Integer doBackgroundTask() {
            return LastfmManager.trackLove(track, action);
          }});
    
    loveTrack.setOnTaskFinishListener(new OnTaskFinishListener<Integer>() {
          @Override public void onTaskFinish(Integer result) {
            if (result == null) {
              result = R.string.error_unknown_error;
            } else {
              getAppContext().getConsole().toast(getString(result));
            }
          }});
    
    loveTrack.execute();
  }
  
  private class PhoneStateListenerExtended extends PhoneStateListener {
    private Player.PlayerState mPreviousPlayerState = null;
    
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
      Player.PlayerState currentPlayerState = getPlayer().getPlayerState();
      if (currentPlayerState == null) {
        Log.e(TAG + " PhoneStateAction.onCallStateChanged(int,String)", "Player state is null");
        return;
      }
      switch(state) {
        case TelephonyManager.CALL_STATE_IDLE:
            if(mPreviousPlayerState == null) {
              break;
            }
            switch(mPreviousPlayerState) {
              case PAUSED:
              case STOPPED:
                  /** do nothing **/
                  break;
              case PLAYING:
                  /** if the player was paused, resume playback **/
                  if (PlayerState.PAUSED.equals(currentPlayerState));
                  getPlayer().pause();
                  getAppContext().getConsole().toast(getString(R.string.phone_state_will_resume));
                  break;
            }
            
            mPreviousPlayerState = null;
            break;
          
        case TelephonyManager.CALL_STATE_OFFHOOK:
        case TelephonyManager.CALL_STATE_RINGING:
          switch(currentPlayerState) {
            case PAUSED:
            case STOPPED:
              /** do nothing **/
              break;
            case PLAYING:
              /** pause the playback if phone starts to ring or the user initiates a call **/
              getPlayer().pause();
              getAppContext().getConsole().toast(getString(R.string.phone_state_will_pause));
              mPreviousPlayerState = currentPlayerState;
              break;
          }
      }
    }
    
  }
  
}
