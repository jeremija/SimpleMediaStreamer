package hr.jsteiner.simplemediastreamer.domain.lastfminfo;

import java.util.Date;
import java.util.List;

public class Album extends AbstractInfoWithImages {
  
  protected String mArtist = null;
  protected String mId = null;
  protected String mMbid = null;
  protected Date mReleaseDate = null;
  protected int mListeners = 0;
  protected int mPlaycount = 0;
  protected List<Track> mTracks = null;
  
  public Album(String name, String url) {
    super(name, url);
  }

  public String getArtist() {
    return mArtist;
  }

  public void setArtist(String artist) {
    mArtist = artist;
  }

  public String getId() {
    return mId;
  }

  public void setId(String id) {
    mId = id;
  }

  public String getMbid() {
    return mMbid;
  }

  public void setMbid(String mbid) {
    mMbid = mbid;
  }

  public Date getReleaseDate() {
    return mReleaseDate;
  }

  public void setReleaseDate(Date releaseDate) {
    mReleaseDate = releaseDate;
  }

  public int getListeners() {
    return mListeners;
  }

  public void setListeners(int listeners) {
    mListeners = listeners;
  }

  public int getPlaycount() {
    return mPlaycount;
  }

  public void setPlaycount(int playcount) {
    mPlaycount = playcount;
  }

  public List<Track> getTracks() {
    return mTracks;
  }

  public void setTracks(List<Track> tracks) {
    mTracks = tracks;
  }

  @Override
  public String toString() {
    return "Album [mName=" + mName + ", mArtist=" + mArtist + ", mId=" + mId
        + ", mMbid=" + mMbid + ", mReleaseDate=" + mReleaseDate + "]";
  }

}
