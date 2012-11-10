package hr.jsteiner.simplemediastreamer.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class RadioTrack implements Parcelable {
  
  private String mLocation;
  private String mTitle;
  private String mIdentifier;
  private String mAlbum;
  private String mCreator;
  private int mDuration;
  private String mImage;
  
  public RadioTrack(String location, String title, String identifier, String album, 
      String creator, int duration, String image) {
    mLocation = location;
    mTitle = title;
    mIdentifier = identifier;
    mAlbum = album;
    mCreator = creator;
    mDuration = duration;
    mImage = image;
  }
  
  public String getLocation() {
    return mLocation;
  }
  public String getTitle() {
    return mTitle;
  }
  public String getIdentifier() {
    return mIdentifier;
  }
  public String getAlbum() {
    return mAlbum;
  }
  public String getCreator() {
    return mCreator;
  }
  public int getDuration() {
    return mDuration;
  }
  public String getImage() {
    return mImage;
  }

  @Override
  public String toString() {
    return "Track [mLocation=" + mLocation + ", mTitle=" + mTitle
        + ", mIdentifier=" + mIdentifier + ", mAlbum=" + mAlbum + ", mCreator="
        + mCreator + ", mDuration=" + mDuration + ", mImage=" + mImage + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mLocation == null) ? 0 : mLocation.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RadioTrack other = (RadioTrack) obj;
    if (mLocation == null) {
      if (other.mLocation != null)
        return false;
    } else if (!mLocation.equals(other.mLocation))
      return false;
    return true;
  }
  
  /*
   * PARCELABLE IMPlEMENTATION
   */

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(mLocation);
    dest.writeString(mTitle);
    dest.writeString(mIdentifier);
    dest.writeString(mAlbum);
    dest.writeString(mCreator);
    dest.writeInt(mDuration);
    dest.writeString(mImage);
  }
  
  public static final Parcelable.Creator<RadioTrack> CREATOR = 
      new Parcelable.Creator<RadioTrack>() {

        @Override
        public RadioTrack createFromParcel(Parcel source) {
          return new RadioTrack(source);
        }

        @Override
        public RadioTrack[] newArray(int size) {
          return new RadioTrack[size];
        }
      };
  
  private RadioTrack(Parcel source) {
    mLocation = source.readString();
    mTitle = source.readString();
    mIdentifier = source.readString();
    mAlbum = source.readString();
    mCreator = source.readString();
    mDuration = source.readInt();
    mImage = source.readString();
  }
}
