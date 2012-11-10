package hr.jsteiner.simplemediastreamer.media;

import hr.jsteiner.simplemediastreamer.ApplicationEx;
import hr.jsteiner.simplemediastreamer.R;
import hr.jsteiner.simplemediastreamer.domain.Playlist;
import hr.jsteiner.simplemediastreamer.domain.RadioTrack;

import java.util.List;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.os.Handler;
import android.util.Log;

public class Player implements OnCompletionListener, OnBufferingUpdateListener, 
OnErrorListener, OnInfoListener{
  
  private static final String TAG = Player.class.getCanonicalName();
  
  private static ApplicationEx mAppContext;
  
  public static void setAppContext(ApplicationEx appContext) {
    mAppContext = appContext;
  }

  private MediaPlayer mMediaPlayer;
  public enum PlayerState {
    STOPPED, PLAYING, PAUSED
  }
  PlayerState mPlayerState;
  
  private int mCurrentTrackDuration;
  
  private int mCurrentTrackIndex;
  private RadioTrack mCurrentTrack;
  private Playlist mPlaylist;
  private boolean mHalfPassedAlreadyNotified;
  
  private Handler mHandler;
  Runnable mMonitorProgress = new Runnable() {
    @Override public void run() {
      onTrackProgressMonitor();
    }
  };
  
  /** listener interfaces **/
  private OnBufferingUpdateListener mOnBufferingUpdateListener = null;
  private OnCompletetionListener mOnCompletetionListener = null;
  private OnPlaybackStateChangedListener mOnPlaybackStateChangedListener = null;
  private OnTrackChangedListener mOnTrackChangedListener = null;
  private OnProgressUpdateListener mOnProgressUpdateListener = null;
  private OnRequestPlaylistUpdateListener mOnRequestPlaylistUpdateListener = null;
  private OnHalfTrackPassedListener mOnHalfTrackPassedListener = null;
  
  
  public Player() {
    
    mMediaPlayer = new MediaPlayer();
    mPlayerState = PlayerState.STOPPED;
    mHandler = new Handler();
    
    mMediaPlayer.setOnBufferingUpdateListener(this);
    mMediaPlayer.setOnCompletionListener(this);
    mMediaPlayer.setOnInfoListener(this);
    mMediaPlayer.setOnErrorListener(this);
  }
  
  public int getCurrentTrackIndex() {
    return mCurrentTrackIndex;
  }

  public void setCurrentTrackIndex(int currentTrackIndex) {
    mCurrentTrackIndex = currentTrackIndex;
  }

  public Playlist getPlaylist() {
    return mPlaylist;
  }

  public void setPlaylist(Playlist playlist) {
    mPlaylist = playlist;
    if (mPlaylist != null && 
        mPlaylist.getTracks() != null &&
        mPlaylist.getTracks().size() > 0) 
    {
      int currentTrackIndex = 0;
      RadioTrack firstTrack = playlist.getTracks().get(0);
      stop();
      play(firstTrack, currentTrackIndex);
    }
  }
  
  public void updatePlaylist(Playlist playlist) {
    if (playlist == null || playlist.getTracks() == null) {
      return;
    }
    
    if (mPlaylist != null && mPlaylist.getTracks() != null) {
      List<RadioTrack> oldTracks = mPlaylist.getTracks();
      List<RadioTrack> newTracks = playlist.getTracks();
       
      oldTracks.addAll(newTracks);
      playlist.setTracks(oldTracks);
    }

    mPlaylist = playlist;
  }
  
  public RadioTrack getCurrentTrack() {
    return mCurrentTrack;
  }

  public void setCurrentTrack(RadioTrack currentTrack, int currentTrackIndex) {
    mCurrentTrack = currentTrack;
    mCurrentTrackIndex = currentTrackIndex;
    
    /*
     * to enable scrobbling on this track!
     */
    mHalfPassedAlreadyNotified = false;
    
    if (mOnTrackChangedListener != null) {
      mOnTrackChangedListener.onTrackChanged(mCurrentTrack, mCurrentTrackIndex);
    }
  }

  private void setPlayerState(PlayerState newState) {
    mHandler.removeCallbacks(mMonitorProgress);
    
    mPlayerState = newState;
    if (mOnPlaybackStateChangedListener != null) {
      mOnPlaybackStateChangedListener.onPlaybackStateChanged(
          mPlayerState, mCurrentTrack, mCurrentTrackDuration, mCurrentTrackIndex);
    }
    
    if (PlayerState.PLAYING.equals(mPlayerState)) {
      onTrackProgressMonitor();
    }
  }
  
  public PlayerState getPlayerState() {
    return mPlayerState;
  }

  public void setOnBufferingUpdateListener(
      OnBufferingUpdateListener onBufferingUpdateListener) {
    mOnBufferingUpdateListener = onBufferingUpdateListener;
  }

  public void setOnCompletetionListener(OnCompletetionListener onCompletetionListener) {
    mOnCompletetionListener = onCompletetionListener;
  }
  
  public void setOnPlaybackStateChangedListener(OnPlaybackStateChangedListener
      onPlaybackStateChangedListener) {
    mOnPlaybackStateChangedListener = onPlaybackStateChangedListener;
  }
  
  public void setOnTrackChangedListener(OnTrackChangedListener onTrackChagnedListener) {
    mOnTrackChangedListener = onTrackChagnedListener;
  }
  
  public void setOnProgressUpdateListener(OnProgressUpdateListener onProgressUpdateListener) {
    mOnProgressUpdateListener = onProgressUpdateListener;
  }
  
  public void setOnRequestPlaylistUpdateListener(
      OnRequestPlaylistUpdateListener onRequestPlaylistUpdateListener)
  {
    mOnRequestPlaylistUpdateListener = onRequestPlaylistUpdateListener;
  }
  
  public void setOnHalfTrackPassedListener(OnHalfTrackPassedListener onHalfTrackPassedListener) {
    mOnHalfTrackPassedListener = onHalfTrackPassedListener;
  }
  
  /** player actions **/
  
  public int play(int trackIndex) {
    if (trackIndex <= mCurrentTrackIndex) {
      return R.string.error_cannot_play_previous_track;
    }
    
    if (mPlaylist == null || mPlaylist.getTracks() == null || mPlaylist.getTracks().size() <= 0) {
      return R.string.error_playlist_is_empty;
    }
    
    if (trackIndex >= mPlaylist.getTracks().size()) {
      return R.string.error_playlist_index_out_of_bounds;
    }
    
    RadioTrack track = mPlaylist.getTracks().get(trackIndex);
    stop();
    return play(track, trackIndex);
  }
  
  /**
   * play a track from playlist if found in playlist, if not founds return an error message
   * @param track
   * @return
   */
  public int play(RadioTrack track) {
    if (mPlaylist == null) {
      return R.string.error_playlist_is_empty;
    }
    
    int trackIndex = mPlaylist.findTrackIndex(track);
    
//    if (trackIndex < 0) {
//      return R.string.error_track_not_found_in_playlist;
//    }
    
    stop();
    return play(track, trackIndex);
  }
  
  /**
   * @return error message resource id, will be 0 if everything ok
   * @param track to play
   * @param trackIndex track index in playlist. set to -1 if not in playlist so that the playback
   * stops after it.
   */
  private int play(RadioTrack track, int trackIndex) {
    int retVal = 0;
    
    if (track == null) return R.string.error_track_is_null;
    setCurrentTrack(track, trackIndex);
    
    requestPlaylistUpdateIfNeeded(trackIndex);
    
    switch(mPlayerState) {
//      case PLAYING:
//            retVal = R.string.error_already_playing;
//            break;
      case PLAYING:
      case STOPPED:
            mMediaPlayer.reset();
            try {
              mMediaPlayer.setDataSource(track.getLocation());
              mMediaPlayer.prepare();
            }
            catch(Exception e) {
              e.printStackTrace();
              mAppContext.getConsole().log(e.toString());
              stop();
              return R.string.error_unknown_error;
            }
            mCurrentTrackDuration = mMediaPlayer.getDuration();
            
            try {
              mMediaPlayer.start();
            }
            catch (IllegalStateException e) {
              e.printStackTrace();
              return R.string.error_illegal_state;
            }
            setPlayerState(PlayerState.PLAYING);
            retVal = 0;
            break;
      case PAUSED:
            try {
              mMediaPlayer.start();
            }
            catch (IllegalStateException e) {
              e.printStackTrace();
              return R.string.error_illegal_state;
            }
            setPlayerState(PlayerState.PLAYING);
            retVal = 0;
            break;
      default:
            retVal = R.string.error_illegal_state;
    }
    
    return retVal;
  }
  
  public int pause() {
    if (PlayerState.PAUSED.equals(mPlayerState)) {
      try {
        mMediaPlayer.start();
      }
      catch(IllegalStateException e) {
        e.printStackTrace();
        return R.string.error_illegal_state;
      }
      setPlayerState(PlayerState.PLAYING);
      return 0;
    }
    
    if (PlayerState.PLAYING.equals(mPlayerState)) {
      try {
        mMediaPlayer.pause();
      }
      catch(IllegalStateException e) {
        e.printStackTrace();
        return R.string.error_illegal_state;
      }
      setPlayerState(PlayerState.PAUSED);
      return 0;
    }
    
    return R.string.error_illegal_state;
  }
  
  public int stop() {
    int result = stopWithoutChangingState();
    setPlayerState(PlayerState.STOPPED);
    return result;
  }
  
  /**
   * This is only used in {@link #nextInPlaylist()} so that the service wouldn't stop in between
   * tracks. Don't use it in any other function
   */
  private int stopWithoutChangingState() {
    if (!PlayerState.STOPPED.equals(mPlayerState)) {
      try {
        mMediaPlayer.stop();
      }
      catch(IllegalArgumentException e) {
        e.printStackTrace();
        return R.string.error_illegal_state;
      }
    }
    return 0;
  }
  
  public int nextInPlaylist() {
    stopWithoutChangingState();
    
    int tracksInPlaylist = 0;
    if (mPlaylist != null && mPlaylist.getTracks() != null) {
      tracksInPlaylist = mPlaylist.getTracks().size();
    }
    Log.d(TAG + "#nextInPlaylist()", "tracksInPlaylist=" + tracksInPlaylist);
    
    int tracksLeftInPlaylist = 0;
    if (mCurrentTrackIndex >= 0) {
      tracksLeftInPlaylist = tracksInPlaylist - 1 - mCurrentTrackIndex;
      Log.d(TAG + "#nextInPlaylist()", "tracksLeftInPlaylist=" + tracksLeftInPlaylist);
      /*
       * switch to next song if any
       */
      if (tracksLeftInPlaylist > 0) {
        int nextTrackIndex = mCurrentTrackIndex + 1;
        Log.d(TAG + "#nextInPlaylist()", 
            "Will try to start the next song at index " + nextTrackIndex);
        RadioTrack nextTrack = mPlaylist.getTracks().get(nextTrackIndex);
        Log.d(TAG + "#nextInPlaylist()", "Next track is: " + nextTrack);
        play(nextTrack, nextTrackIndex);
        
        return 0;
      }
    }
    return R.string.error_no_next_track;
    
  }
  
  private void requestPlaylistUpdateIfNeeded(int trackIndex) {
    if (mPlaylist == null || mPlaylist.getTracks() == null || mPlaylist.getTracks().size() <= 0) {
      return;
    }
    
    int playlistItems = mPlaylist.getTracks().size();
    int maxIndex = playlistItems - 1;
    
    if (trackIndex >= maxIndex - 2 && mOnRequestPlaylistUpdateListener != null) {
      Log.d(TAG + "#requestPlaylistUpdateIfNeeded(int)", "requesting playlistUpdate!");
      mOnRequestPlaylistUpdateListener.onRequestPlaylistUpdate();
    }
  }
  
  public void onTrackProgressMonitor() {
    if ( !PlayerState.PLAYING.equals(mPlayerState) ) {
      return;
    }
    
    int currentPosition = mMediaPlayer.getCurrentPosition();
    int percent = 0;
    if (mCurrentTrackDuration > 0) {
      percent = (int)  ( (float) currentPosition / mCurrentTrackDuration * 100);
    }
    
    if (mOnProgressUpdateListener != null) {
      mOnProgressUpdateListener.onProgressUpdate(currentPosition, percent, mCurrentTrackDuration);
    }
    
    if (percent > 50 && 
        currentPosition > 15 * 1000 &&
        mCurrentTrackDuration > 30 * 1000 &&
        mHalfPassedAlreadyNotified == false && 
        mOnHalfTrackPassedListener != null)
    {
      mHalfPassedAlreadyNotified = true;
      mOnHalfTrackPassedListener.onHalfTrackPassed(mCurrentTrack);
    }
    
    mHandler.postDelayed(mMonitorProgress, 1000);
  }

  /** overrides **/
  
  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {
    if (mOnBufferingUpdateListener == null) {
      return;
    }
    mOnBufferingUpdateListener.onBufferingUpdate(percent);
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    
    if (nextInPlaylist() == 0) {
      /*
       * if started the next song successfully
       */
      return;
    }
    
    /*
     * Playback has stopped
     */
    Log.d(TAG + "#onCompletion(MediaPlayer)", "playback has stopped");
    setPlayerState(PlayerState.STOPPED);

    if (mOnCompletetionListener != null) {
      mOnCompletetionListener.onCompletetion();
    }
  }
  
  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra) {
    mAppContext.getConsole().log("MediaPlayer info (" + what + "," + extra + ")");
    return true;
  }
  
  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    mAppContext.getConsole().log("MediaPlayer error (" + what + "," + extra + ")");
    return true;
  }
  
  /** interfaces **/
  
  public interface OnBufferingUpdateListener {
    public void onBufferingUpdate(int percent);
  }
  
  public interface OnCompletetionListener {
    public void onCompletetion();
  }
  
  public interface OnPlaybackStateChangedListener {
    public void onPlaybackStateChanged(PlayerState state, RadioTrack track, int duration, 
        int currentTrackIndex);
  }
  
  public interface OnTrackChangedListener {
    public void onTrackChanged(RadioTrack track, int trackIndex);
  }
  
  public interface OnProgressUpdateListener {
    public void onProgressUpdate(int time, int percent, int duration);
  }
  
  public interface OnRequestPlaylistUpdateListener {
    public void onRequestPlaylistUpdate();
  }
  
  public interface OnHalfTrackPassedListener {
    public void onHalfTrackPassed(RadioTrack track);
  }

  
  
}
