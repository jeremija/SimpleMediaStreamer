package hr.jsteiner.simplemediastreamer;

import hr.jsteiner.simplemediastreamer.adapters.PlaylistAdapter;
import hr.jsteiner.simplemediastreamer.domain.Playlist;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PlaylistActivity extends Activity {
  
  private ListView mPlaylistView = null;
  private Context mContext = this;
  
  int mCurrentTrackIndex;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.playlist_layout);
    initView();
    
    Intent intent = getIntent();
    Playlist playlist = (Playlist) intent.getParcelableExtra("playlist");
    Integer curTrack = intent.getIntExtra("currentTrackIndex", -1);
    if (curTrack != null) {
      mCurrentTrackIndex = curTrack.intValue();
    }
    
    if (playlist == null) {
      return;
    }
    
    /*
     *  TODO implement nowplaying position in constructor so that the currently playing entry can
     *  be coloured.
     */
    ListAdapter playlistAdapter = new PlaylistAdapter(mContext, playlist.getTracks(), 
        mCurrentTrackIndex);
    mPlaylistView.setAdapter(playlistAdapter);
  }
  
  private void initView() {
    mPlaylistView = (ListView) findViewById(R.id.playlistView);
  }
  

  
}
