package hr.jsteiner.simplemediastreamer.util;

import hr.jsteiner.common.domain.xml.XmlTag;
import hr.jsteiner.simplemediastreamer.ApplicationEx;
import hr.jsteiner.simplemediastreamer.domain.LastfmError;
import hr.jsteiner.simplemediastreamer.domain.Playlist;
import hr.jsteiner.simplemediastreamer.domain.RadioTrack;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.AbstractInfo;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.AbstractInfoWithImages;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Album;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Artist;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.ArtistDetailed;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Tag;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Track;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.text.Html;
import android.util.Log;

//import android.util.Log;

public class LastfmXmlUtil {
  
  public static final String TAG = LastfmXmlUtil.class.getCanonicalName();
  
  public static ApplicationEx mAppContext = null;
  
  public static void setAppContext(ApplicationEx appContext) {
    mAppContext = appContext;
  }

  /**
   * Create a Playlist object with tracks by parsing the xml.
   * @param xml
   * @return Playlist with tracks from xml or null if xml is null.
   */
  public static Playlist getPlaylistFromXml(XmlTag playlistTag) {
    if (playlistTag == null) {
      return null;
    }
    
    String playlistTitle = playlistTag.getChildByNameValue("title");
    String playlistCreator = playlistTag.getChildByNameValue("creator");
    Date playlistDate = parseLastfmDate(playlistTag.getChildByNameValue("date"));

    int playlistExpiry = 0;
    try {
      playlistExpiry = Integer.parseInt(playlistTag.getChildByNameValue("link"));
    }
    catch(NumberFormatException e) {
      e.printStackTrace();
    }
    
    Playlist radioPlaylist = new Playlist(playlistTitle, playlistCreator, playlistDate, 
        playlistExpiry);
    
    XmlTag tracks = playlistTag.getChildByName("trackList");
    XmlTag trackTag = tracks.getChildByNameAvoidDuplicates("track");
    while (trackTag != null) {
      String location = trackTag.getChildByNameValue("location");
      String title = trackTag.getChildByNameValue("title");
      String identifier = trackTag.getChildByNameValue("identifier");
      String album = trackTag.getChildByNameValue("album");
      String creator = trackTag.getChildByNameValue("creator");
      
      if (title != null) title = Html.fromHtml(title).toString();
      if (album != null) album = Html.fromHtml(album).toString();
      if (creator != null) creator = Html.fromHtml(creator).toString();
      
      int duration = 0;
      try {
        duration = Integer.parseInt(trackTag.getChildByNameValue("duration"));
      }
      catch (NumberFormatException e) {
        e.printStackTrace();
      }
      
      String image = trackTag.getChildByNameValue("image");
      
      RadioTrack track = new RadioTrack(location, title, identifier, album, creator, duration, image);
      radioPlaylist.addTrack(track);
      
      /** for next iteration **/
      trackTag = tracks.getChildByNameAvoidDuplicates("track");
    }
    
    return radioPlaylist;
  }
  
  public static void setImagesToAbstractInfoWithImages(AbstractInfoWithImages info, XmlTag tag) {
    if (info == null || tag == null) {
      return;
    }
    
    String imageSmall = tag.getChildByNameAndAttributeValue("image", "size", "small");
    String imageMedium = tag.getChildByNameAndAttributeValue("image", "size", "medium");
    String imageLarge = tag.getChildByNameAndAttributeValue("image", "size", "large");
    String imageXLarge = tag.getChildByNameAndAttributeValue("image", "size", "extralarge");
    String imageMega = tag.getChildByNameAndAttributeValue("image", "size", "mega");
    
    info.setImages(imageSmall, imageMedium, imageLarge, imageXLarge, imageMega);
  }
  
  public static void setWikiInfoToAbstractInfo(AbstractInfo info, Map<String, Object> wikiMap) {
    if (info == null || wikiMap == null) {
      return;
    }
    
    Date published = null;
    String summary = null;
    String content = null;
    
    if (wikiMap != null) {
      published = (Date) wikiMap.get("published");
      summary = (String) wikiMap.get("summary");
      content = (String) wikiMap.get("content");
    }
    
    info.setTextInfo(published, summary, content);
  }

  public static ArtistDetailed getArtistInfoFromXml(XmlTag artistTag) {
    if (artistTag == null) {
      return null;
    }

    String name = artistTag.getChildByNameValue("name");
    if (name != null) name = Html.fromHtml(name).toString();
    String mbid = artistTag.getChildByNameValue("mbid");
    String url = artistTag.getChildByNameValue("url");
//    String imageSmall = artistTag.getChildByNameAndAttributeValue("image", "size", "small");
//    String imageMedium = artistTag.getChildByNameAndAttributeValue("image", "size", "medium");
//    String imageLarge = artistTag.getChildByNameAndAttributeValue("image", "size", "large");
//    String imageXLarge = artistTag.getChildByNameAndAttributeValue("image", "size", "extralarge");
//    String imageMega = artistTag.getChildByNameAndAttributeValue("image", "size", "mega");
    boolean streamable = "1".equals(artistTag.getChildByNameValue("streamable")) ? true : false;
    int listeners = 0;
    int plays = 0;
    try {
      listeners = Integer.parseInt(artistTag.getChildByNameValue("listeners"));
      plays = Integer.parseInt(artistTag.getChildByNameValue("plays"));
    }
    catch (NumberFormatException e) {
//      Log.e(TAG, e.toString());
    }
    catch (NullPointerException e) {
      // do nothing
    }
    
    /**
     * SIMILAR ARTISTS
     */
    
    XmlTag similarTag = artistTag.getChildByName("similar");
    
    List<Artist> similarArtists = getSimilarArtistsFromXml(similarTag);
    
    /**
     * TAGS
     */
    
    XmlTag tagsTag = artistTag.getChildByName("tags");
    
    List<Tag> tags = new ArrayList<Tag>();
    if (tagsTag != null) {
      XmlTag singleTag = tagsTag.getChildByNameAvoidDuplicates("tag");
      while (singleTag != null) {
        Tag tag = getTagFromXml(singleTag);
        tags.add(tag);
        
        /** for next iteration **/
        singleTag = tagsTag.getChildByNameAvoidDuplicates("tag");
      }
    }
    
    XmlTag bioTag = artistTag.getChildByName("bio");
    Map<String, Object> bioMap = getWikiTextFromXml(bioTag);
    
    
    ArtistDetailed artistDetail = new ArtistDetailed(name, mbid, url, streamable, listeners, plays, 
        similarArtists, tags);
    setImagesToAbstractInfoWithImages(artistDetail, artistTag);
    setWikiInfoToAbstractInfo(artistDetail, bioMap);
//    artistDetail.setImages(imageSmall, imageMedium, imageLarge, imageXLarge, imageMega);
    
    return artistDetail;
  }
  
  
  public static Tag getTagFromXml(XmlTag singleTag) {
    if (singleTag == null) {
      return null;
    }
    
    String tagName = singleTag.getChildByNameValue("name");
    if (tagName != null) Html.fromHtml(tagName).toString();
    String tagUrl = singleTag.getChildByNameValue("url");
    
    int reach = 0;
    int taggings = 0;
    try {
      reach = Integer.parseInt(singleTag.getChildByNameValue("reach"));
      taggings = Integer.parseInt(singleTag.getChildByNameValue("taggings"));
    }
    catch(NumberFormatException e) {
      // do nothing
    }
    catch(NullPointerException e) {
      // do nothing
    }
    boolean streamable = "1".equals(singleTag.getChildByName("streamable"))?true:false;
    
    XmlTag wikiTag = singleTag.getChildByName("wiki");
    Map<String, Object> wikiMap = getWikiTextFromXml(wikiTag);
    
    Date published = null;
    String summary = null;
    String content = null;
    
    if (wikiMap != null) {
      published = (Date) wikiMap.get("published");
      summary = (String) wikiMap.get("summary");
      content = (String) wikiMap.get("content");
    }
    
    Tag tag = 
        new Tag(tagName, tagUrl, reach, taggings, streamable, published, summary, content);
    
    return tag;
  }
  
  public static List<Tag> getTagsFromXml(XmlTag tagsTag) {    
    List<Tag> tags = new ArrayList<Tag>();
    if (tagsTag != null) {
      XmlTag singleTag = tagsTag.getChildByNameAvoidDuplicates("tag");
      while (singleTag != null) {
        Tag tag = getTagFromXml(singleTag);
        tags.add(tag);
        
        /*
         * for next iteration
         */
        singleTag = tagsTag.getChildByNameAvoidDuplicates("tag");
      }
    }
    
    if (tags.size() > 0) {
      return tags;
    }
    return null;
  }
  
  public static Map<String, Object> getWikiTextFromXml(XmlTag wikiTag) {
    if (wikiTag == null) {
      return null;
    }
    Date published =  parseLastfmDateLong(wikiTag.getChildByNameValue("published"));
    String summary = wikiTag.getChildByNameValue("summary");
    if (summary != null) summary =
        summary.replace("\n", "<br/>");
    String content = wikiTag.getChildByNameValue("content");
    if (content != null) content =
        content.replace("\n", "<br/>");
    
    Map<String, Object> wiki = new HashMap<String, Object>();
    wiki.put("published", published);
    wiki.put("summary", summary);
    wiki.put("content", content);
    
    return wiki;
  }
  
  public static Artist getSimilarArtistFromXml(XmlTag artistTag) {
    
    String similarName = artistTag.getChildByNameValue("name");
    if (similarName != null) similarName = Html.fromHtml(similarName).toString();
    String mbid = artistTag.getChildByNameValue("mbid"); // this is not always present
    String similarUrl = artistTag.getChildByNameValue("url");
//    String similarImageSmall = artistTag.getChildByNameAndAttributeValue("image", "size", "small");
//    String similarImageMedium = artistTag.getChildByNameAndAttributeValue("image", "size", "medium");
//    String similarImageLarge = artistTag.getChildByNameAndAttributeValue("image", "size", "large");
//    String similarImageXLarge = artistTag.getChildByNameAndAttributeValue("image", "size", "extralarge");
//    String similarImageMega = artistTag.getChildByNameAndAttributeValue("image", "size", "mega");
    
    Artist similarArtist = new Artist(similarName, similarUrl, mbid);
    setImagesToAbstractInfoWithImages(similarArtist, artistTag);
    
    return similarArtist;
  }
  
  public static List<Artist> getSimilarArtistsFromXml(XmlTag similarArtistsTag) {
    List<Artist> artists = new ArrayList<Artist>();
    
    if (similarArtistsTag != null) {
      XmlTag artistTag = similarArtistsTag.getChildByNameAvoidDuplicates("artist");
      while (artistTag != null) {
        Artist artist = LastfmXmlUtil.getSimilarArtistFromXml(artistTag);
        if (artist != null) {
          artists.add(artist);
        }
        artistTag = similarArtistsTag.getChildByNameAvoidDuplicates("artist");
      }
    }
    
    if (artists.size() > 0) {
      return artists;
    }
    
    return null;
  }
  
  public static Album getAlbumFromXml(XmlTag albumTag) {
    if (albumTag == null) {
      return null;
    }
    
    String title = albumTag.getChildByNameValue("name");
    if (title == null) title = albumTag.getChildByNameValue("title");
    
    Album album = new Album(title, 
        albumTag.getChildByNameValue("url"));
    
    album.setArtist(albumTag.getChildByNameValue("artist"));
    album.setId(albumTag.getChildByNameValue("id"));
    album.setMbid(albumTag.getChildByNameValue("mbid"));
    album.setUrl(albumTag.getChildByNameValue("url"));
    album.setReleaseDate(parseLastfmDateAlbum(albumTag.getChildByNameValue("releasedate")));
    
    setImagesToAbstractInfoWithImages(album, albumTag);
    int listeners = 0;
    int playcount = 0;
    try {
      listeners = Integer.parseInt(albumTag.getChildByNameValue("listeners"));
      playcount = Integer.parseInt(albumTag.getChildByNameValue("playcount"));
    }
    catch (NumberFormatException e) { }
    catch (NullPointerException e) { }
    
    album.setListeners(listeners);
    album.setPlaycount(playcount);
    
    album.setTracks(getTracksFromXml(albumTag.getChildByName("tracks")));
    album.setTags(getTagsFromXml(albumTag.getChildByName("toptags")));
    Map<String, Object> wikiMap = getWikiTextFromXml(albumTag.getChildByName("wiki"));
    setWikiInfoToAbstractInfo(album, wikiMap);
    
    return album;
  }
  
//  public static List<Album> getAlbumsFromXml(XmlTag albumsTag) {
//    List<Album> albums = new ArrayList<Album>();
//    if (albumsTag != null) {
//      XmlTag singleAlbum = albumsTag.getChildByNameAvoidDuplicates("album");
//      while (singleAlbum != null) {
//        Album album = getAlbumFromXml(singleAlbum);
//        albums.add(album);
//        
//        /*
//         * for next iteration
//         */
//        singleAlbum = albumsTag.getChildByNameAvoidDuplicates("album");
//      }
//    }
//    
//    if (albums.size() > 0) {
//      return albums;
//    }
//    return null;
//  }
  
  public static Track getTrackFromXml(XmlTag trackTag) {
    if (trackTag == null) {
      return null;
    }
    
    String id = trackTag.getChildByNameValue("id");
    String name = trackTag.getChildByNameValue("name");
    if (name != null) name = Html.fromHtml(name).toString();
    String mbid = trackTag.getChildByNameValue("mbid");
    String url = trackTag.getChildByNameValue("url");
    boolean streamable = "1".equals(trackTag.getChildByNameValue("streamable"))?true:false;
    int trackNumber = 0;
    int duration = 0;
    int listeners = 0;
    int playcount = 0;
    try {
      trackNumber = Integer.parseInt(trackTag.getAttribute("rank"));
      duration = Integer.parseInt(trackTag.getChildByNameValue("duration"));
      listeners = Integer.parseInt(trackTag.getChildByNameValue("listeners"));
      playcount = Integer.parseInt(trackTag.getChildByNameValue("playcount"));
    }
    catch(NumberFormatException e) { }
    catch(NullPointerException e) { }
    
    
    Artist artist = getSimilarArtistFromXml(trackTag.getChildByName("artist"));
    Album album = getAlbumFromXml(trackTag.getChildByName("album"));
    List<Tag> tags = getTagsFromXml(trackTag.getChildByName("toptags"));
    Map<String, Object> wiki = getWikiTextFromXml(trackTag.getChildByName("wiki"));
    
    Date published = null;
    String summary = null;
    String content = null;
    
    if (wiki != null) {
      published = (Date) wiki.get("published");
      summary = (String) wiki.get("summary");
      content = (String) wiki.get("content");
    }
    
    Track track = new Track(name, url);
    track.setId(id);
    track.setMbid(mbid);
    track.setStreamable(streamable);
    track.setTrackNumber(trackNumber);
    track.setDuration(duration);
    track.setListeners(listeners);
    track.setPlaycount(playcount);
    track.setArtist(artist);
    track.setAlbum(album);
    track.setTags(tags);
    track.setPublished(published);
    track.setSummary(summary);
    track.setContent(content);
    
    return track;
  }
  
  public static List<Track> getTracksFromXml(XmlTag tracksTag) {
    List<Track> tracks = new ArrayList<Track>();
    if (tracksTag != null) {
      XmlTag singleTrack = tracksTag.getChildByNameAvoidDuplicates("track");
      while (singleTrack != null) {
        Track track = getTrackFromXml(singleTrack);
        tracks.add(track);
        
        /*
         * for next iteration
         */
        singleTrack = tracksTag.getChildByNameAvoidDuplicates("track");
      }
    }
    
    if (tracks.size() > 0) {
      return tracks;
    }
    return null;
  }
  
  /**
   * Check last.fm XML result for error messages.
   * @param xml
   * @return error message or null if everything ok
   */
  public static LastfmError checkXmlTagForErrorMessages(XmlTag xmlTag) {
    if (xmlTag == null) {
      Log.e(TAG + "#checkXmlTagForErrorMessages(XmlTag)", "xmlTag is null");
      return new LastfmError("error", -1, "Xml result is null!");
    }
    
    String lastfmStatusValue = xmlTag.getAttribute("status");
    
    if ("ok".equals(lastfmStatusValue)) {
      /** everything is ok **/
      return null;
    }
    
    /** an error happened **/
    
    XmlTag errorTag = xmlTag.getChildByName("error");
    String errorCode = errorTag.getAttribute("code");
    String errorMessage = errorTag.getValue();
    
    LastfmError error = new LastfmError(lastfmStatusValue, errorCode, errorMessage);
    if (error!= null) {
      Log.e(TAG + "#checkXmlTagForErrorMessages(XmlTag)", "" +
      		"Lastfm errorCode " + error.getErrorCode() + ": " + error.getErrorMessage());
    }
    return error;
  }

  public static Date parseLastfmDate(String date) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date playlistDate = null;
    try {
      playlistDate = dateFormat.parse(date);
    }
    catch (ParseException e) {
      e.printStackTrace();
    }
    return playlistDate;
  }
  
  public static Date parseLastfmDateLong(String date) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss ZZZZ");
    Date parsedDate = null;
    try {
      parsedDate = dateFormat.parse(date);
    }
    catch(ParseException e) {
      e.printStackTrace();
    }
    return parsedDate;
  }
  
  public static Date parseLastfmDateAlbum(String date) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
    Date parsedDate = null;
    try {
      parsedDate = dateFormat.parse(date);
    }
    catch(ParseException e) {
      e.printStackTrace();
    }
    return parsedDate;
  }
  
  public static String parseLastfmUrl(String url) {
    if (url == null) {
      return null;
    }
    
    String parsed = url.replaceAll(".*?www.last.fm/.*?/", "");
    parsed = parsed.replaceAll("/.*", "");
    
    String decoded = null;
    try {
      decoded = URLDecoder.decode(parsed, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    
    return decoded;
  }
}
