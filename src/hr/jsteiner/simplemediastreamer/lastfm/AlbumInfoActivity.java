package hr.jsteiner.simplemediastreamer.lastfm;

import hr.jsteiner.simplemediastreamer.R;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Album;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Track;
import hr.jsteiner.simplemediastreamer.managers.LastfmManager;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class AlbumInfoActivity extends LastfmInfoActivityAbstract<Album> {

  private String mInfoParamArtist = null;
  private String mInfoParamAlbum= null;
  
  @Override
  protected void setRadioUrl() {
    setRadioUrl(null, null);
  }

  protected void popupTracksDialog(final List<Track> tracks) {
    if (tracks == null) {
      getAppContext().getConsole().toast(getString(R.string.feature_not_available_here));
      return;
    }
    
    final Dialog dialog = new Dialog(this);
    dialog.setTitle(getString(R.string.info_tracks_from_album));
    
    final Context context = this;
    
    ListView tracksView = new ListView(context);
    ListAdapter tracksAdapter = new ArrayAdapter<Track>(this, R.layout.album_track_layout, tracks);
    tracksView.setAdapter(tracksAdapter);

    dialog.setContentView(tracksView);
    tracksView.setOnItemClickListener(new OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Track track = tracks.get((int) id);
        
        if (track != null && track.getArtist() != null) {
          InfoSearchParam isp = 
              InfoSearchParam.generateTrackSearchParam(track.getArtist().getName(), track.getName());
          isp.startActivity(context);
        }
        dialog.dismiss();
      }
    });
    
    dialog.show();
  }

  @Override
  protected void setInfo(Album album, boolean fetchImages) {
    mInfo = album;
    if (mInfo == null) {
      return;
    }
    
    if (fetchImages) {
      fetchImages(getLargestImageUrl(album), null);
    } else {
      dismissProgressDialog();
    }
    
    setTitle(album.getName());
    setTypeTitle(getString(R.string.info_type_album));
    
    setContent(album.getContent());
    
    inflateTags(album.getTags());
  }
  
  @Override
  protected void handleIntent(Intent intent) {
    if (intent == null) {
      return;
    }
    
    String action = intent.getAction();
    if (Intent.ACTION_VIEW.equals(action)) {
      /* this will later be used in doBackgroundTask() method */
      mInfoParamArtist = intent.getStringExtra("artist");
      mInfoParamAlbum = intent.getStringExtra("album");
      fetchInfo();
    }
  }

  @Override
  public void addCustomItemsMenu(Menu menu) {
    MenuItem popupTracksItem = menu.add(R.string.info_menu_view_tracks);
    popupTracksItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(MenuItem item) {
        if (mInfo != null) {
          popupTracksDialog(mInfo.getTracks());
        }
        return true;
      }
    });
  }

  @Override
  public Album doBackgroundTask() {
    return LastfmManager.albumGetInfo(mInfoParamArtist, mInfoParamAlbum);
  }

}
