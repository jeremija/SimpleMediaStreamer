package hr.jsteiner.simplemediastreamer.domain.lastfminfo;

public class Artist extends AbstractInfoWithImages {
  protected String mMbid;

  public Artist(String name, String url, String mbid) {
    super(name, url);
    mMbid = mbid;
  }
  
  public String getMbid() {
    return mMbid;
  }
  public void setMbid(String mbid) {
    mMbid = mbid;
  }

  @Override
  public String toString() {
    return "Artist [mName=" + mName + ", mMbid=" + mMbid + "]";
  }

}
