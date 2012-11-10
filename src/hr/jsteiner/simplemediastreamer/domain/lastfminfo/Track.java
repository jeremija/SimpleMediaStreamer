package hr.jsteiner.simplemediastreamer.domain.lastfminfo;



public class Track extends AbstractInfo {
  
  String mId = null;
  String mMbid = null;
  int mDuration = 0;
  boolean mStreamable = false;
  int mListeners = 0;
  int mPlaycount = 0;
  Artist mArtist = null;
  Album mAlbum = null;
  
  int mTrackNumber = 0;
  
  public Track(String name, String url) {
    super(name, url);
  }

  public String getId() {
    return mId;
  }

  public void setId(String id) {
    mId = id;
  }

  public int getDuration() {
    return mDuration;
  }

  public void setDuration(int duration) {
    mDuration = duration;
  }

  public boolean isStreamable() {
    return mStreamable;
  }

  public void setStreamable(boolean streamable) {
    mStreamable = streamable;
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

  public Artist getArtist() {
    return mArtist;
  }

  public void setArtist(Artist artist) {
    mArtist = artist;
  }
  
  public String getMbid() {
    return mMbid;
  }

  public void setMbid(String mbid) {
    mMbid = mbid;
  }

  public Album getAlbum() {
    return mAlbum;
  }

  public void setAlbum(Album albums) {
    mAlbum = albums;
  }

  public int getTrackNumber() {
    return mTrackNumber;
  }

  public void setTrackNumber(int trackNumber) {
    mTrackNumber = trackNumber;
  }
  
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    if (mTrackNumber != 0) {
      buffer.append(mTrackNumber).append(". "); 
    }
    buffer.append(mName);
    return buffer.toString();
  }

}
