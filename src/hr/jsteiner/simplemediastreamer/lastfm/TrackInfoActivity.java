package hr.jsteiner.simplemediastreamer.lastfm;

import hr.jsteiner.simplemediastreamer.R;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Track;
import hr.jsteiner.simplemediastreamer.managers.LastfmManager;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

public class TrackInfoActivity extends LastfmInfoActivityAbstract<Track> {
  
  private String mInfoParamArtist = null;
  private String mInfoParamTrack = null;
  
  @Override
  protected void setRadioUrl() {
    setRadioUrl(null, null);
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
      mInfoParamTrack = intent.getStringExtra("track");
      fetchInfo();
    }
    
  }

  @Override
  protected void setInfo(Track track, boolean fetchImages) {
    mInfo = track;
    if (mInfo == null) {
      return;
    }
    
    if (fetchImages && track.getAlbum() != null) {
      fetchImages(getLargestImageUrl(track.getAlbum()), null);
    } else {
      dismissProgressDialog();
    }
    setTitle(track.getName());
    setTypeTitle(getString(R.string.info_type_track));
    
    setContent(track.getContent());
    
    inflateTags(track.getTags());
  }

  @Override
  public void addCustomItemsMenu(Menu menu) {
    final Context context = this;
    
    MenuItem openArtistItem = menu.add(R.string.info_menu_open_artist);
    openArtistItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(MenuItem item) {
        if (mInfo != null & mInfo.getArtist() != null) {
          InfoSearchParam ifs = 
              InfoSearchParam.generateArtistSearchParam(mInfo.getArtist().getName());
          if (ifs != null) ifs.startActivity(context);
        }
        return true;
      }
    });
    
    MenuItem openAlbumItem = menu.add(R.string.info_menu_open_album);
    openAlbumItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(MenuItem item) {
        if (mInfo != null & mInfo.getArtist() != null && mInfo.getAlbum() != null) {
          InfoSearchParam ifs = InfoSearchParam.generateAlbumSearchParam(
              mInfo.getArtist().getName(), mInfo.getAlbum().getName());
          if (ifs != null) ifs.startActivity(context);
        }
        return true;
      }
    });
  }

  @Override
  public Track doBackgroundTask() {
    return LastfmManager.trackGetInfo(mInfoParamArtist, mInfoParamTrack);
  }
  
  

}
