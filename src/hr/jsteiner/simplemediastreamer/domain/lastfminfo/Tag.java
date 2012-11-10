package hr.jsteiner.simplemediastreamer.domain.lastfminfo;

import java.util.Date;
import java.util.List;

public class Tag extends AbstractInfo {
  
  protected int mReach;
  protected int mTaggings;
  
  /*
   * Similar tags
   */
  protected List<Artist> mArtists;
  
  public Tag(String name, String url) {
    super(name, url);
  }
  
  public Tag(String name, String url, int reach, int taggings,
      boolean streamable, Date published, String summary, String content)
  {
    super(name, url);
    mReach = reach;
    mTaggings = taggings;
    
    setTextInfo(published, summary, content);
    setStreamable(streamable);
  }

  public int getReach() {
    return mReach;
  }

  public void setReach(int reach) {
    mReach = reach;
  }

  public int getTaggings() {
    return mTaggings;
  }

  public void setTaggings(int taggings) {
    mTaggings = taggings;
  }

  public List<Artist> getArtists() {
    return mArtists;
  }
  public void setArtists(List<Artist> artists) {
    this.mArtists = artists;
  }

  @Override
  public String toString() {
    return "Tag [mName=" + mName + "]";
  }
  
}
