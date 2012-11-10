package hr.jsteiner.simplemediastreamer.lastfm;

import hr.jsteiner.simplemediastreamer.R;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Tag;
import hr.jsteiner.simplemediastreamer.managers.LastfmManager;
import android.content.Intent;
import android.view.Menu;

public class TagInfoActivity extends LastfmInfoActivityAbstract<Tag>{

  private static final String RADIO_PATH = "tag/";
  private static final String RADIO_PATH2 = null;
  
  private String mInfoParamTag = null;
  
  @Override
  protected void setInfo(Tag tag, boolean fetchImages) {
    mInfo = tag;
    if (mInfo == null) {
      return;
    }
    
    if (fetchImages && tag.getArtists() != null && tag.getArtists().size() > 0) {
      fetchImages(getLargestImageUrl(tag.getArtists().get(0)), tag.getArtists());
    }
    else {
      dismissProgressDialog();
    }
    setTitle(tag.getName());
    setTypeTitle(getString(R.string.info_type_tag));
    
    setContent(tag.getContent());
    
    inflateTags(tag.getTags());
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
      mInfoParamTag = intent.getStringExtra("tag");
      fetchInfo();
    }
  }

  @Override
  public void addCustomItemsMenu(Menu menu) {
    /*
     * no additional menus here
     */
  }

  @Override
  public Tag doBackgroundTask() {
    return LastfmManager.tagGetInfo(mInfoParamTag);
  }

}
