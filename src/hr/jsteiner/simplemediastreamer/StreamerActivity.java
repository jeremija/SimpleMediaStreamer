package hr.jsteiner.simplemediastreamer;

import hr.jsteiner.common.util.StringUtil;
import hr.jsteiner.simplemediastreamer.adapters.PlaylistAdapter;
import hr.jsteiner.simplemediastreamer.async.AsyncTaskCallback.OnTaskFinishListener;
import hr.jsteiner.simplemediastreamer.async.DownloadImagesTask;
import hr.jsteiner.simplemediastreamer.domain.Playlist;
import hr.jsteiner.simplemediastreamer.domain.RadioTrack;
import hr.jsteiner.simplemediastreamer.domain.Station;
import hr.jsteiner.simplemediastreamer.lastfm.InfoSearchParam;
import hr.jsteiner.simplemediastreamer.managers.DataManager;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class StreamerActivity extends Activity implements OnClickListener, OnTouchListener {
  
  private static final int SEEKBAR_MAX = 99;
  private static final String SAMPLE_STREAM_URL = "http://www.robtowns.com/music/blind_willie.mp3";
  private static final String TAG = StreamerActivity.class.getCanonicalName();
    
  private TextView mTextTitle = null;
  private TextView mTextAuthor = null;
  private TextView mTextAlbum = null;
  private TextView mTextTime = null;
  private TextView mTextTimeProgress = null;
  private ImageButton mButtonPlay = null;
  private ImageButton mButtonPause = null;
  private ImageButton mButtonStop = null;
  private ImageButton mButtonNext = null;
  private Button mButtonLove = null;
  private Button mButtonUnlove = null;
  private Button mButtonBan = null;
  private Button mButtonUnban = null;
  private ImageView mTrackImage = null;
  private EditText mEditUrl = null;
  private SeekBar mSeekbar = null;
  
  private RadioTrack mCurrentTrack = null;
  
  public ApplicationEx getAppContext() {
    return (ApplicationEx) getApplicationContext();
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {    
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.streamer_layout);
    initView();
    
    /**
     * if an external application started this activity
     */
    Intent intent = getIntent();
    String action = intent.getAction();
    Log.d(TAG, "action=" + intent.getAction());
    
    if (Intent.ACTION_VIEW.equals(action)) {
      Log.d(TAG + "#onCreate(Bundle)", "intent.getData().toString=" + intent.getData().toString());
      popupTuneIn(intent.getData().toString());
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    
    /** if the service was running and the activity closed, restore the currently playing track **/
    Intent getCurrentTrackIntent = new Intent(StreamerService.ACTION_GET_CURRENT_TRACK);
    /** expect a broadcast with action StreamerService.BROADCAST_TRACK **/
    startService(getCurrentTrackIntent);
    
    Log.v(TAG + "#onStart()", "StreamerActivity started. Hello!");
  }
  
  private void stopService() {
    Intent i=new Intent(this, StreamerService.class);
    stopService(i);
  }
  
  @Override
  protected void onResume() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(StreamerService.BROADCAST_BUFFER_UPDATE);
    filter.addAction(StreamerService.BROADCAST_TRACK_PROGRESS);
    filter.addAction(StreamerService.BROADCAST_PLAYLIST);
    filter.addAction(StreamerService.BROADCAST_TRACK);
    
    registerReceiver(mReceiver, filter);
    
    super.onResume();
  }

  @Override
  protected void onPause() {
    unregisterReceiver(mReceiver);
    super.onPause();
  }


  @Override
  protected void onDestroy() {
//    stopService();
    Log.v(TAG + "#onDestroy()", "StreamerActivity destroyed.");
    super.onDestroy();
  }

  private void initView() {
    mTextTitle = (TextView) findViewById(R.id.textTitle);
    mTextAuthor = (TextView) findViewById(R.id.textAuthor);
    mTextAlbum = (TextView) findViewById(R.id.textAlbum);
    mTextTimeProgress = (TextView) findViewById(R.id.textTimeProgress);
    mTextTime = (TextView) findViewById(R.id.textTime);
    mButtonPlay = (ImageButton) findViewById(R.id.buttonPlay);
    mButtonPause = (ImageButton) findViewById(R.id.buttonPause);
    mButtonStop = (ImageButton) findViewById(R.id.buttonStop);
    mButtonNext = (ImageButton) findViewById(R.id.buttonNext);
    mButtonLove = (Button) findViewById(R.id.buttonLove);
    mButtonUnlove = (Button) findViewById(R.id.buttonUnlove);
    mButtonBan = (Button) findViewById(R.id.buttonBan);
    mButtonUnban = (Button) findViewById(R.id.buttonUnban);
    mTrackImage = (ImageView) findViewById(R.id.trackImage);
    mEditUrl = (EditText) findViewById(R.id.editUrl);
    mSeekbar = (SeekBar) findViewById(R.id.seekProgress);
    
    mButtonPlay.setOnClickListener(this);
    mButtonPause.setOnClickListener(this);
    mButtonStop.setOnClickListener(this);
    mButtonNext.setOnClickListener(this);
    mButtonLove.setOnClickListener(this);
    mButtonUnlove.setOnClickListener(this);
    mButtonBan.setOnClickListener(this);
    mButtonUnban.setOnClickListener(this);
    mEditUrl.setText(SAMPLE_STREAM_URL);
    mSeekbar.setMax(SEEKBAR_MAX);
    mSeekbar.setOnTouchListener(this);
    mSeekbar.setEnabled(false);
  }
  
  private void setTrackInfo(RadioTrack track) {
    mCurrentTrack = track;
    
    if (getAppContext().getLastTunedStation() != null) {
      setTitle(getAppContext().getLastTunedStation().getName());
    }
    if (track != null) {
      mTextTitle.setText(track.getTitle());
      mTextAuthor.setText(track.getCreator());
      mTextAlbum.setText(track.getAlbum());
      mEditUrl.setText(track.getLocation());
      
      DownloadImagesTask task = new DownloadImagesTask();
      task.setOnTaskFinishListener(new OnTaskFinishListener<List<Bitmap>>() {
        @Override public void onTaskFinish(List<Bitmap> result) {
          if (result != null && result.size() > 0) {
            mTrackImage.setImageBitmap(result.get(0));
          }
        }
      });
      task.execute(track.getImage());
      
      String totalTime = StringUtil.milisecondsToMinutesAndSeconds(track.getDuration());
      mTextTime.setText(totalTime);
    }
  }
  
  @Override
  public boolean onTouch(View v, MotionEvent event) {
//    if (v.getId() == R.id.seekProgress) {
//      if(mMediaPlayer.isPlaying()) {
//        int seekToMilis = mMediaDuration / 100 * mSeekbar.getProgress();
//        mMediaPlayer.seekTo(seekToMilis);
//      }
//    }
    return false;
  }
  
  public void trackChanged(RadioTrack track, int trackIndex) {
    setTrackInfo(track);
  }
  
  public void progressUpdate(int time, int percent, int duration) {
    mSeekbar.setProgress(percent);
    
    String currentTime = StringUtil.milisecondsToMinutesAndSeconds(time);
    mTextTimeProgress.setText(currentTime);
  }
  
  public void bufferUpdate(int percent) {
    mSeekbar.setSecondaryProgress(percent);
  }
  
  @Override
  public void onClick(View v) {
    Log.d(TAG + "#onClick()", "view id = " + v.getId());
    
    if (v.getId() == R.id.buttonPlay) {
      InfoSearchParam ifs = InfoSearchParam.fromLastfmUrl("http://www.last.fm/music/Eric+Clapton/The+Cream+Of+Clapton");
      if (ifs != null) ifs.startActivity(this);
    }
    
    if(v.getId() == R.id.buttonPause) {
      Intent pauseIntent = new Intent(StreamerService.ACTION_PAUSE);
      startService(pauseIntent);
    }
    
    if(v.getId() == R.id.buttonStop) {
      Intent stopIntent = new Intent(StreamerService.ACTION_STOP);
      startService(stopIntent);
    }
    
    if(v.getId() == R.id.buttonNext) {
      Intent nextIntent = new Intent(StreamerService.ACTION_NEXT);
      startService(nextIntent);
    }
    
    if (v.getId() == R.id.buttonLove ||
        v.getId() == R.id.buttonUnlove ||
        v.getId() == R.id.buttonBan ||
        v.getId() == R.id.buttonUnban)
    {
      String loveAction = null;
      if(v.getId() == R.id.buttonLove) {
        loveAction = "love";
      }
      
      if(v.getId() == R.id.buttonUnlove) {
        loveAction = "unlove";
      }
      
      if(v.getId() == R.id.buttonBan) {
        loveAction = "ban";
      }
      
      if(v.getId() == R.id.buttonUnban) {
        loveAction = "unban";
      }
      
      Intent loveIntent = new Intent(StreamerService.ACTION_LOVE_TRACK);
      loveIntent.putExtra("loveAction", loveAction);
      startService(loveIntent);
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.streamer_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case R.id.streamer_menu_login:
        Log.d(TAG + "#onOptionsItemSelected(MenuItem)", 
            "will now start the LastfmAuthenticatorActivity");
        Intent lastfmAuthenticateIntent = new Intent(this, LastfmLoginActivity.class);
        startActivity(lastfmAuthenticateIntent);
        return true;
      case R.id.streamer_menu_tune:
        popupTuneIn(null);
        return true;
      case R.id.streamer_menu_playlist:
        Intent getPlaylist = new Intent(StreamerService.ACTION_GET_PLAYLIST);
        /** expect a broadcast with action StreamerService.BROADCAST_PLAYLIST **/ 
        startService(getPlaylist);
        return true;
      case R.id.streamer_menu_test:
        if (mTextAuthor.getText() != null) {
          InfoSearchParam ifs = InfoSearchParam.generateArtistSearchParam(
              mTextAuthor.getText().toString());
          if (ifs != null) ifs.startActivity(this);
//          Intent artistInfoIntent = new Intent(this, ArtistInfoActivity.class);
//          artistInfoIntent.setAction(Intent.ACTION_VIEW);
//          artistInfoIntent.putExtra("info", mTextAuthor.getText().toString());
//          startActivity(artistInfoIntent);
        }
        return true;
      case R.id.streamer_menu_console:
        popupConsole();
        return true;
      case R.id.streamer_menu_about:
        popupAbout();
        return true;
      case R.id.streamer_menu_close:
        stopService();
        finish();
        return true;
      default: 
        return super.onOptionsItemSelected(item);
    }
  }
  
  /**
   * @param stationUrl initial lastfm:// url to display
   */
  private void popupTuneIn(String stationUrl) {
    final String stationUrlToDisplay;
    if (stationUrl != null) {
      stationUrlToDisplay = stationUrl;
    }
    else {
      stationUrlToDisplay = getString(R.string.streamer_station_url_default);
    }
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.tunein_title);
    
    LayoutInflater inflater = getLayoutInflater();
    View tuneInView = inflater.inflate(R.layout.tunein_layout, null);
    
    /**
     *  station input (url or parameter) 
     */
    final AutoCompleteTextView stationParamsInput = 
        (AutoCompleteTextView) tuneInView.findViewById(R.id.tuneinStationParamsInput);
    String[] previousUrls = DataManager.loadPreviousStationsList();
    ArrayAdapter<String> previousUrlsAdapter= new ArrayAdapter<String>(this, 
        android.R.layout.simple_dropdown_item_1line, previousUrls);
    stationParamsInput.setAdapter(previousUrlsAdapter);
    stationParamsInput.setText(stationUrlToDisplay);
    stationParamsInput.setSelection(stationParamsInput.length());
    
    /**
     * popup history button
     */
    final ImageButton buttonPopupHistory = 
        (ImageButton) tuneInView.findViewById(R.id.buttonPopupHistory);
    buttonPopupHistory.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        stationParamsInput.showDropDown();
      }
    });
    
    /**
     * spinner
     */
    final Spinner stationType = (Spinner) tuneInView.findViewById(R.id.spinnerTuneinStationType);
    ArrayAdapter<CharSequence> stationTypeAdapter = ArrayAdapter.createFromResource(this,
        R.array.tunein_station_type, android.R.layout.simple_spinner_item);
    stationTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    stationType.setAdapter(stationTypeAdapter);
    stationType.setSelection(0);
    
    stationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view,
          int position, long id) {
        if (position == 0) {
          stationParamsInput.setText(stationUrlToDisplay);
          stationParamsInput.setSelection(stationParamsInput.length());
          stationParamsInput.setHint(getString(R.string.tunein_station_input_hint_url));
          buttonPopupHistory.setEnabled(true);
        }
        if (position != 0) {
          stationParamsInput.setText("");
          stationParamsInput.setSelection(stationParamsInput.length());
          buttonPopupHistory.setEnabled(false);
          switch(position) {
            case 1:
            case 2:
              stationParamsInput.setHint(getString(R.string.tunein_station_input_hint_artist));
              break;
            case 3:
            case 4:
            case 5:
            case 6:
              stationParamsInput.setHint(getString(R.string.tunein_station_input_hint_user));
              break;
            case 7:
            default:
              stationParamsInput.setHint(getString(R.string.tunein_station_input_hint_tag));
              break;
          }
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        /** do nothing **/
      }
      
    });
    
    builder.setView(tuneInView);
    builder.setNegativeButton(getString(R.string.tunein_negative), null);
    builder.setPositiveButton(getString(R.string.tunein_positive), 
        new DialogInterface.OnClickListener() 
    {
      @Override public void onClick(DialogInterface dialog, int which) {
        int stationTypeInt = stationType.getSelectedItemPosition();
        StringBuffer stationUrl = new StringBuffer(getString(R.string.streamer_station_url_default));
        switch(stationTypeInt) {
          case 0:
            stationUrl = new StringBuffer(stationParamsInput.getText().toString());
            break;
          case 1:
            /** lastfm://artist/cher/similarartists **/ 
            stationUrl.append("artist/");
            stationUrl.append(stationParamsInput.getText().toString());
            stationUrl.append("/similarartists");
            break;
          case 2:
            /** lastfm://artist/cher/fans **/
            stationUrl.append("artist/");
            stationUrl.append(stationParamsInput.getText().toString());
            stationUrl.append("/fans");
            break;
          case 3:
            /** lastfm://user/last.hq/library **/
            stationUrl.append("user/");
            stationUrl.append(stationParamsInput.getText().toString());
            stationUrl.append("/library");
            break;
          case 4:
            /** lastfm://user/last.hq/mix **/
            stationUrl.append("user/");
            stationUrl.append(stationParamsInput.getText().toString());
            stationUrl.append("/mix");
            break;
          case 5:
            /** lastfm://user/last.hq/recommended **/
            stationUrl.append("user/");
            stationUrl.append(stationParamsInput.getText().toString());
            stationUrl.append("/recommended");
            break;
          case 6:
            /** lastfm://user/last.hq/neighbours **/
            stationUrl.append("user/");
            stationUrl.append(stationParamsInput.getText().toString());
            stationUrl.append("/neighbours");
            break;
          case 7:
            /** lastfm://globaltags/disco **/
            stationUrl.append("globaltags/");
            stationUrl.append(stationParamsInput.getText().toString());
            break;
          case 8:
            /** lastfm://tag/rock*80s **/
            stationUrl.append("tag/");
            stationUrl.append(stationParamsInput.getText().toString());
            break;
        }
        
        String tuneStationUrl = stationUrl.toString();
        getAppContext().getConsole().toast("url=" + tuneStationUrl);
        
        Intent tuneIntent = new Intent(StreamerService.ACTION_TUNE_IN);
        tuneIntent.putExtra("stationUrl", tuneStationUrl);
        startService(tuneIntent);
      }
    });
    
    AlertDialog alertDialog = builder.create();
//    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//      @Override public void onShow(DialogInterface dialog) {
//        stationParamsInput.showDropDown();
//      }
//    });
    alertDialog.show();
  }
  
  private void popupConsole() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.console);
    LayoutInflater inflater = getLayoutInflater();
    View consoleView = inflater.inflate(R.layout.console_layout, null);
    final TextView consoleText = (TextView) consoleView.findViewById(R.id.consoleText);
    final ScrollView consoleScroll = (ScrollView) consoleView.findViewById(R.id.consoleScroll);
    final Runnable consoleScrollDown =new Runnable() {
      @Override public void run() {
        consoleScroll.fullScroll(View.FOCUS_DOWN);
      }};
    
    consoleText.setText(getAppContext().getConsole().getFullLog());
    consoleScroll.post(consoleScrollDown);

    consoleText.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        consoleText.setText(getAppContext().getConsole().getFullLog());
        consoleText.append("\nRefresh " + System.currentTimeMillis());
        consoleScroll.post(consoleScrollDown);
        return true;
      }
    });
    
    builder.setView(consoleView);
    builder.setNegativeButton(getString(R.string.console_clear), 
        new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            getAppContext().getConsole().clearLog();
            consoleText.setText("");
          }
    });
    
    
    builder.setPositiveButton(getString(R.string.console_close), null);
    
    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }
  
  private void popupAbout() {
    final Dialog dialog = new Dialog(this);
    dialog.setTitle(R.string.about);
    dialog.setContentView(R.layout.about_layout);
    
    
    EditText editStationName = (EditText) dialog.findViewById(R.id.editStationName);
    EditText editStationType = (EditText) dialog.findViewById(R.id.editStationType);
    EditText editStationUrl = (EditText) dialog.findViewById(R.id.editStationUrl);
    EditText editStationDiscovery = (EditText) dialog.findViewById(R.id.editStationDiscovery);
    EditText editCurrentTrackUrl = (EditText) dialog.findViewById(R.id.editCurrentTrackUrl);
    Station station = null;
    if ((station = getAppContext().getLastTunedStation()) != null) {
      editStationName.setText(station.getName());
      editStationType.setText(station.getType());
      editStationUrl.setText(station.getUrl());
      editStationDiscovery.setText(station.getSupportsDiscovery() ? "true" : "false");
    }
    if (mCurrentTrack != null) {
      editCurrentTrackUrl.setText(mCurrentTrack.getLocation());
    }
    
    dialog.show();
  }
  
  public void popupPlaylist(Playlist playlist, int currentTrackIndex) {
    if (playlist == null || playlist.getTracks() == null) {
      getAppContext().getConsole().toast(getString(R.string.playlist_empty));
      return;
    }
    
    final Dialog dialog = new Dialog(this);
    dialog.setTitle(R.string.playlist);
    dialog.setContentView(R.layout.playlist_layout);
    
    ListView playlistView = (ListView) dialog.findViewById(R.id.playlistView);
    ListAdapter playlistAdapter = 
        new PlaylistAdapter(this, playlist.getTracks(), currentTrackIndex);
    playlistView.setAdapter(playlistAdapter);
    playlistView.setOnItemClickListener(new OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(StreamerService.ACTION_NEXT_WITH_INDEX);
        intent.putExtra("trackIndex", (int) id);
        startService(intent);
        dialog.dismiss();
      }
    });
    
    if (currentTrackIndex >= 0) {
      playlistView.setSelection(currentTrackIndex);
    }
    
    dialog.show();
  }
  
  
  BroadcastReceiver mReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      
      if (StreamerService.BROADCAST_PLAYLIST.equals(action)) {
        Playlist playlist = (Playlist) intent.getParcelableExtra("playlist");
        int currentTrackIndex = intent.getIntExtra("currentTrackIndex", -1);
        popupPlaylist(playlist, currentTrackIndex);
      }
      
      if (StreamerService.BROADCAST_TRACK.equals(action)) {
        RadioTrack track = (RadioTrack) intent.getParcelableExtra("track");
        setTrackInfo(track);
      }
      
      if (StreamerService.BROADCAST_TRACK_PROGRESS.equals(action)) {
        int time = intent.getIntExtra("time", 0);
        int percent = intent.getIntExtra("percent", 0);
        int duration = intent.getIntExtra("duration", 0);
        progressUpdate(time, percent, duration);
      }

      if (StreamerService.BROADCAST_BUFFER_UPDATE.equals(action)) {
        int percent = intent.getIntExtra("percent", 0);
        mSeekbar.setSecondaryProgress(percent);
      }
    }
    
  };
  
}
