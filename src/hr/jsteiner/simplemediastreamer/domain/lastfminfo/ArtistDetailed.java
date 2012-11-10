package hr.jsteiner.simplemediastreamer.domain.lastfminfo;

import java.util.List;

public class ArtistDetailed extends Artist {
  int mListeners;
  int mPlays;
  List<Artist> mSimilarArtists;

  public ArtistDetailed(String name, String mbid, String url, 
      boolean streamable, int listeners, int plays, List<Artist> similarArtists, 
      List<Tag> tags)
  {
    super(name, mbid, url);
    mStreamable = streamable;
    mListeners = listeners;
    mPlays = plays;
    mSimilarArtists = similarArtists;
    mTags = tags;
    
  }

  public int getListeners() {
    return mListeners;
  }

  public void setListeners(int listeners) {
    mListeners = listeners;
  }

  public int getPlays() {
    return mPlays;
  }

  public void setPlays(int plays) {
    mPlays = plays;
  }

  public List<Artist> getSimilarArtists() {
    return mSimilarArtists;
  }

  public void setSimilarArtists(List<Artist> similarArtists) {
    mSimilarArtists = similarArtists;
  }

  @Override
  public String toString() {
    return "ArtistDetail [mName=" + mName + ", mUrl=" + mUrl + ", mMbid="
        + mMbid + ", mImageSmall=" + mImageSmall + ", mImageMedium="
        + mImageMedium + ", mImageLarge=" + mImageLarge + ", mImageXLarge="
        + mImageXLarge + ", mImageMega=" + mImageMega + "]";
  }

}
