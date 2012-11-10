package hr.jsteiner.simplemediastreamer.lastfm;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class InfoSearchParam implements Parcelable {
  
  private static final String LASTFM_URL = "http://www.last.fm/";
  
  private static final String PATH_TAG = "tag/";
  private static final String PATH_ARTIST_ALBUM_OR_TRACK = "music/";
  
  private static final int CHECK_LEVELS = 4;
  
  
  public static final int SEARCH_TYPE_TAG = 10;
  public static final int SEARCH_TYPE_ARTIST = 11;
  public static final int SEARCH_TYPE_ALBUM = 12;
  public static final int SEARCH_TYPE_TRACK = 13;
  
  private int mSearchType;
  
  private String mArtist = null;
  private String mAlbum = null;
  private String mTrack = null;
  private String mTag = null;
  
  private InfoSearchParam() {
    
  }
  
  public String constructLastfmUrl() {
    StringBuffer buffer = new StringBuffer(LASTFM_URL);
    
    switch(mSearchType) {
      case SEARCH_TYPE_TAG:
        buffer.append(PATH_TAG);
        buffer.append(mTag);
        break;
      case SEARCH_TYPE_ARTIST:
        buffer.append(PATH_ARTIST_ALBUM_OR_TRACK);
        buffer.append(mArtist);
        break;
      case SEARCH_TYPE_ALBUM:
        buffer.append(PATH_ARTIST_ALBUM_OR_TRACK);
        buffer.append(mArtist).append("/");
        buffer.append(mAlbum);
        break;
      case SEARCH_TYPE_TRACK:
        buffer.append(PATH_ARTIST_ALBUM_OR_TRACK);
        buffer.append(mArtist).append("/_/");
        buffer.append(mTrack);
        break;
      default:
    }
    
    return buffer.toString();
  }
  
  public void startActivity(Context context) {
    if (context == null) {
      return;
    }
    
    Intent intent = new Intent(Intent.ACTION_VIEW);
    
    switch (mSearchType) {
      case SEARCH_TYPE_TAG:
        intent.setClass(context, TagInfoActivity.class);
        intent.putExtra("tag", mTag);
        context.startActivity(intent);
        return;
      case SEARCH_TYPE_ARTIST:
        intent.setClass(context, ArtistInfoActivity.class);
        intent.putExtra("artist", mArtist);
        context.startActivity(intent);
        return;
      case SEARCH_TYPE_ALBUM:
        intent.setClass(context, AlbumInfoActivity.class);
        intent.putExtra("artist", mArtist);
        intent.putExtra("album", mAlbum);
        context.startActivity(intent);
        return;
      case SEARCH_TYPE_TRACK:
        intent.setClass(context, TrackInfoActivity.class);
        intent.putExtra("artist", mArtist);
        intent.putExtra("track", mTrack);
        context.startActivity(intent);
        return;
      default:
        return;
    }
  }
  
  /*
   * FACTORY METHODS
   */
  
  public static InfoSearchParam fromLastfmUrl(String url) {
    String path = url.replaceAll(".*?www.last.fm", "");
    
    Pattern p = Pattern.compile("(?<=/)[^/]*");
    Matcher m = p.matcher(path);
    
    String[] level = new String[CHECK_LEVELS];
    
    for (int i = 0; i < CHECK_LEVELS && m.find(); i++) {
      level[i] = m.group();
    }
    
    String level0 = level[0];
    
    if (level0 == null) {
      return null;
    }
    
    if ("music".equals(level0)) {
      /*
       * do stuff for music
       */
      String artist = level[1];
      String album = level[2];
      String track = level[3];
      
      if (artist == null) {
        return null;
      }
      
      if (album == null) {
          try {
            artist = URLDecoder.decode(artist, "UTF-8");
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }
        return generateArtistSearchParam(artist);
      }
      
      if (track == null) {
        try {
          artist = URLDecoder.decode(artist, "UTF-8");
          album = URLDecoder.decode(album, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
        return generateAlbumSearchParam(artist, album);
      }
      
      try {
        artist = URLDecoder.decode(artist, "UTF-8");
        album = URLDecoder.decode(album, "UTF-8");
        track = URLDecoder.decode(track, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return generateTrackSearchParam(artist, track);
    }
    
    if ("tag".equals(level0)) {
      String tag = level[1];
      try {
        tag = URLDecoder.decode(tag, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return generateTagSearchParam(tag);
    }
    
    return null;
  }
  
  public static InfoSearchParam generateArtistSearchParam(String artist) {
    if (artist == null) {
      return null;
    }
    
    InfoSearchParam isp = new InfoSearchParam();
    isp.mArtist = artist;
    isp.mSearchType = SEARCH_TYPE_ARTIST;
    
    return isp;
  }
  
  public static InfoSearchParam generateAlbumSearchParam(String artist, String album) {
    if (artist == null || album == null) {
      return null;
    }
    
    InfoSearchParam isp = new InfoSearchParam();
    isp.mArtist = artist;
    isp.mAlbum = album;
    isp.mSearchType = SEARCH_TYPE_ALBUM;
    
    return isp;
  }
  
  public static InfoSearchParam generateTrackSearchParam(String artist, String track) {
    if (artist == null || track == null) {
      return null;
    }
    
    InfoSearchParam isp = new InfoSearchParam();
    isp.mArtist = artist;
    isp.mTrack = track;
    isp.mSearchType = SEARCH_TYPE_TRACK;
    
    return isp;
  }
  
  public static InfoSearchParam generateTagSearchParam(String tag) {
    if (tag == null) {
      return null;
    }
    
    InfoSearchParam isp = new InfoSearchParam();
    isp.mTag = tag;
    isp.mSearchType = SEARCH_TYPE_TAG;
    
    return isp;
  }

  /*
   * GETTERS
   */
  
  public int getInfoSearchType() {
    return mSearchType;
  }

  public String getArtist() {
    return mArtist;
  }

  public String getAlbum() {
    return mAlbum;
  }

  public String getTrack() {
    return mTrack;
  }

  public String getTag() {
    return mTag;
  }

  
  /*
   * PARCELABLE IMPLEMENTATION
   */
  
  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(mArtist);
    dest.writeString(mAlbum);
    dest.writeString(mTrack);
    dest.writeString(mTag);
    dest.writeInt(mSearchType);
  }
  
  public static final Parcelable.Creator<InfoSearchParam> CREATOR = 
      new Parcelable.Creator<InfoSearchParam>() 
  {

    @Override public InfoSearchParam createFromParcel(Parcel source) {
      return new InfoSearchParam(source);
    }

    @Override public InfoSearchParam[] newArray(int size) {
      return new InfoSearchParam[size];
    }
    
  };
  
  private InfoSearchParam(Parcel source) {
    mArtist = source.readString();
    mAlbum = source.readString();
    mTrack = source.readString();
    mTag = source.readString();
    mSearchType = source.readInt();
  }
}
