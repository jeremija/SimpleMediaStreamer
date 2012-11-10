package hr.jsteiner.simplemediastreamer.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Playlist implements Parcelable{
  
  private String mTitle;
  private String mCreator;
  private Date mDate;
  private int mExpiry;
  
  private List<RadioTrack> mTracks;

  public Playlist(String title, String creator, Date date, int expiry) {
    mTitle = title;
    mCreator = creator;
    mDate = date;
    mExpiry = expiry;
    
    mTracks = new ArrayList<RadioTrack>();
  }
  
  public List<RadioTrack> getTracks() {
    return mTracks;
  }
  
  /**
   * Adds a track to the mTracks list. Instantiates mTracks to avoid NullPointerException
   * @param track if null will not be added
   */
  public void addTrack(RadioTrack track) {
    if (mTracks == null) {
      mTracks = new ArrayList<RadioTrack>();
    }
    if (track != null) {
      mTracks.add(track);
    }
  }

  public void setTracks(List<RadioTrack> tracks) {
    mTracks = tracks;
  }

  public String getTitle() {
    return mTitle;
  }

  public String getCreator() {
    return mCreator;
  }

  public Date getDate() {
    return mDate;
  }

  public int getExpiry() {
    return mExpiry;
  }

  @Override
  public String toString() {
    return "Playlist [mTitle=" + mTitle + ", mCreator=" + mCreator + ", mDate="
        + mDate + ", mExpiry=" + mExpiry + ", mTracks=" + mTracks + "]";
  }
  
  /**
   * @param track to search for
   * @return first found track index if found, -1 if not found
   */
  public int findTrackIndex(RadioTrack track) {
    if (mTracks == null || mTracks.size() <= 0) {
      return -1;
    }
    
    return mTracks.indexOf(track);
    
  }
  
  /*
   * PARCELABLE
   */

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(mTitle);
    dest.writeString(mCreator);
    dest.writeLong(mDate.getTime());
    dest.writeInt(mExpiry);
    
    dest.writeTypedList(mTracks);
  }
  
  public static final Parcelable.Creator<Playlist> CREATOR =
      new Parcelable.Creator<Playlist>() {

        @Override
        public Playlist createFromParcel(Parcel source) {
          return new Playlist(source);
        }

        @Override
        public Playlist[] newArray(int size) {
          return new Playlist[size];
        }
    
      };
  
  private Playlist(Parcel source) {
    mTitle = source.readString();
    mCreator = source.readString();
    mDate = new Date(source.readLong());
    mExpiry = source.readInt();
    
    if (mTracks == null) {
      mTracks = new ArrayList<RadioTrack>();
    }
    source.readTypedList(mTracks, RadioTrack.CREATOR);
  }
}
