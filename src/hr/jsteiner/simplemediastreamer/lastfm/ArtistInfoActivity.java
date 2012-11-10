package hr.jsteiner.simplemediastreamer.lastfm;

import hr.jsteiner.simplemediastreamer.R;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.ArtistDetailed;
import hr.jsteiner.simplemediastreamer.managers.LastfmManager;
import android.content.Intent;
import android.view.Menu;

public class ArtistInfoActivity extends LastfmInfoActivityAbstract<ArtistDetailed> {
  
  private static final String RADIO_PATH = "artist/";
  private static final String RADIO_PATH2 = "/similarartists";
  
  private String mInfoParamArtist = null;
  
  @Override
  protected void setInfo(ArtistDetailed artistDetail, boolean fetchImages) {
    mInfo = artistDetail;
    if (artistDetail == null) {
      return;
    }
    
    if (fetchImages) {
      fetchImages(getLargestImageUrl(artistDetail), artistDetail.getSimilarArtists());
    } else {
      dismissProgressDialog();
    }
    
    setTitle(artistDetail.getName());
    setTypeTitle(getString(R.string.info_type_artist));
    
    setContent(artistDetail.getContent());
    
    inflateTags(artistDetail.getTags());
  }

  @Override
  protected void setRadioUrl() {
    setRadioUrl(RADIO_PATH, RADIO_PATH2);
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
      fetchInfo();
    }
  }

  @Override
  public void addCustomItemsMenu(Menu menu) {
    /*
     * No additional menus here
     */
  }

  @Override
  public ArtistDetailed doBackgroundTask() {
    return LastfmManager.artistGetInfo(mInfoParamArtist);
  }
  
}
