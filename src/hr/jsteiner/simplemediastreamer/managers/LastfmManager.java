package hr.jsteiner.simplemediastreamer.managers;

import hr.jsteiner.common.domain.Exceptions.WebPageException;
import hr.jsteiner.common.domain.xml.XmlTag;
import hr.jsteiner.common.util.Md5Util;
import hr.jsteiner.common.util.WebUtil;
import hr.jsteiner.common.util.XmlUtil;
import hr.jsteiner.simplemediastreamer.ApplicationEx;
import hr.jsteiner.simplemediastreamer.R;
import hr.jsteiner.simplemediastreamer.domain.LastfmError;
import hr.jsteiner.simplemediastreamer.domain.LastfmUser;
import hr.jsteiner.simplemediastreamer.domain.Playlist;
import hr.jsteiner.simplemediastreamer.domain.Station;
import hr.jsteiner.simplemediastreamer.domain.RadioTrack;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Album;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Artist;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.ArtistDetailed;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Tag;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Track;
import hr.jsteiner.simplemediastreamer.util.LastfmXmlUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

/**
 * This is the main class for calling the methods from the lastfm api. Almost all methods require
 * a parameter called api_sig which is the method signature. Basically all required (non-optional)
 * parameters (key-value pairs, except api_sig) are sorted alphabetically by the key and then they 
 * are concattenated.
 * <br/><br/>
 * For example, if you have 3 parameters:<br/> 
 *    api_key = api_value<br/>
 *    method = lastfm.method<br/>
 *    token = token_value<br/>
 * <br/>
 * the resulting concatenated string would be "api_keyapi_valuemethodlastfm.methodtokentoken_value".
 * Append the API_SECRET value to this key and generate a MD5 hash of 32 hex characters and that is
 * the value for api_sig.
 * <br/><br/> 
 * AUTHENTICATION:<br/>
 * This class enables you to authenticate like a desktop application. The authentication has
 * three steps:
 *   <ol>
 *     <li>fetchRequestToken()</li>
 *     <li>requestAuthorizationFromUser()<br/>
 *          Redirect the user to this url so that he can authenticate the application online.<br/>
 *          </li>
 *     <li>createWebServiceSession()<br/>
 *          If successful this returns a sessionKey which is stored in the LastfmUser object along<br/>
 *          with other info.<br/></li>
 *   </ol>
 * 
 * There is also a helper method paramsToRest which returns the parameters in a single string
 * which is handy using in a GET or POST requests.
 * <br/>
 * 
 * @author jsteiner
 *
 */
public class LastfmManager {
  
  private static final String TAG = LastfmManager.class.getCanonicalName();
  
  public static ApplicationEx mAppContext = null;
  
  public static void setAppContext(ApplicationEx appContext) {
    mAppContext = appContext;
  }

  private static final String USER_AUTH_URL = "http://www.last.fm/api/auth/?api_key=";
  
  private static final String LASTFM_API_URL = "http://ws.audioscrobbler.com/2.0/";
  
  private static final String AUTH_GETTOKEN = "auth.gettoken";
  private static final String AUTH_GETSESSION = "auth.getSession";
  private static final String RADIO_TUNE = "radio.tune";
  private static final String RADIO_GETPLAYLIST = "radio.getPlaylist";
  private static final String TRACK_UPDATE_NOWPLAYING = "track.updateNowPlaying";
  private static final String TRACK_SCROBBLE = "track.scrobble";
  private static final String ARTIST_GETINFO = "artist.getInfo";
  private static final String TAG_GETINFO = "tag.getInfo";
  private static final String TAG_GETTOP_ARTISTS = "tag.getTopArtists";
  private static final String TAG_GETSIMILAR = "tag.getSimilar";
  private static final String TRACK_GETINFO = "track.getInfo";
  private static final String ALBUM_GETINFO = "album.getInfo";
  
  private static final String API_KEY = "generate-your-own";
  private static final String API_SECRET = "generate-your-own";
  
  /**
   * 
   * @param xmlTag to check for error status
   * @return false when everything is ok, true when error happened
   */
  public static boolean genericIsLastfmError(XmlTag xmlTag) {
    LastfmError error = LastfmXmlUtil.checkXmlTagForErrorMessages(xmlTag);
    
    if (error != null) {
      Log.e(TAG + "#genericIsLastfmError(XmlTag)", "error=" + error);
      mAppContext.getConsole().toast("error=" + error);
      return true;
    }
    return false;
  }
  
  /**
   * 
   * @param params to create the String from
   * @return GET or POST compatible params String
   */
  public static String paramsToRest(Map<String, String> params) {
    if (params == null) {
      throw new IllegalArgumentException();
    }
    
    urlEncodeAllParameterValues(params);
    
    StringBuffer paramsBuffer = new StringBuffer();
    
    Iterator<Entry<String, String>> it = params.entrySet().iterator();
    int i = 0;
    while(it.hasNext()) {
      Entry<String, String> pair = it.next();
      if (i > 0) {
        paramsBuffer.append("&");
      }
      paramsBuffer.append(pair.getKey() + "=" + pair.getValue());
      i++;
    }
    
    Log.v(TAG + "#paramsToRest(Map)", "paramsToRest + " + paramsBuffer.toString());
    
    return paramsBuffer.toString();
  }
  
  private static void urlEncodeAllParameterValues(Map<String, String> params) {
    Iterator<Entry<String, String>> it = params.entrySet().iterator();
    while(it.hasNext()) {
      Entry<String, String> entry = it.next();
      String value = entry.getValue();
      try {
        if (value != null) {
          String encodedValue = URLEncoder.encode(value, "UTF-8");
          entry.setValue(encodedValue);
        }
      }
      catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Calls last.fm method with GET request and return the result (usually an XML file).
   * @param params
   * @return
   */
  public static XmlTag callLastfmMethod(Map<String, String> params) {
    StringBuffer urlBuffer = new StringBuffer(LASTFM_API_URL + "?");
    
    urlBuffer.append(paramsToRest(params));
    String url = urlBuffer.toString();
    Log.d(TAG + "#callLastfmMethod(Map)", "callLastfmMethod(): url=" + url);
    String resultingXml = null;
    resultingXml = WebUtil.downloadPageToString(url);
    Log.v(TAG + "#callLastfmMethod(Map)", "callLastfmMethod(): resultingXml=" + resultingXml);
    XmlTag xmlTag = XmlUtil.parseXml(resultingXml);
    
    return xmlTag;
  }
  
  /**
   * Calls last.fm method with POST request and return the result (usually an XML file).
   * @param params
   * @return
   */
  public static XmlTag callLastfmMethodPost(Map<String, String> params) {
    Log.d(TAG + "#callLastfmMethodPost(Map)", "params=" + params.toString());
    String restParams = paramsToRest(params);
    String resultingXml = null;
    resultingXml = WebUtil.downloadPageToStringWithPostMethod(LASTFM_API_URL, restParams);
    Log.v(TAG + "#callLastfmMethodPost(Map)", "resultingXml=" + resultingXml);
    XmlTag xmlTag = XmlUtil.parseXml(resultingXml);
    
    return xmlTag;
  }
  
  /**
   * Generates method signature for params append it to them.
   * @param method to be called
   * @param params should be only non-optional parameters ???
   */
  public static void generateMethodSignature(Map<String, String> params) {
    
    StringBuffer urlBuffer = new StringBuffer();
    /*
     * concat the parameter-value pair
     */
    Iterator<Entry<String, String>> it = params.entrySet().iterator(); 
    List<String> parameterList = new ArrayList<String>();
    while(it.hasNext()) {
      Entry<String, String> pair = it.next();
      parameterList.add(pair.getKey() + pair.getValue());
    }
    
    /*
     * sort it alphabeticallty
     */
    Collections.sort(parameterList);
    for (String parameterAndValue : parameterList) {
      urlBuffer.append(parameterAndValue);
    }
    
    urlBuffer.append(API_SECRET);
    
    String signature = urlBuffer.toString();
    Log.v(TAG + "#generateMethodSignature(Map)", "signature=" + signature);
    String signatureMd5 = Md5Util.generateHash(signature);
    Log.v(TAG + "#generateMethodSignature(Map)", "signature=" + signatureMd5);
    
    params.put("api_sig", signatureMd5);
    
  }

  /**
   * Last.fm Authentication API - http://www.last.fm/api/authspec
   * 4.1 Fetch a request token
   * @return token
   * @throws WebPageException
   */
  public static String fetchRequestToken() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("api_key", API_KEY);
    params.put("method", AUTH_GETTOKEN);
    
    XmlTag result = callLastfmMethod(params);
    Log.d(TAG + "#fetchRequestToken()", "pageContent=" + result);
    
    if (genericIsLastfmError(result)) {
      return null;
    }
    
    XmlTag tokenTag = result.getChildByName("token");
    
    String token = tokenTag.getValue();
    
    Log.d(TAG + "#fetchRequestToken()", "mToken=" + token + "\n");
    
    return token;
  }
  
  /**
   * Last.fm Authentication API - http://www.last.fm/api/authspec
   * 4.2 Request authorization from the user
   * @param token
   * @return url to send the user to
   */
  public static String requestAuthorizationFromUser(String token) {
    String url = USER_AUTH_URL + API_KEY + "&token=" + token;
    Log.d(TAG + "#requestAuthorizationFromUser(String)", "url=" + url);
    return url;
  }
  
  /**
   * Last.fm Authentication API - <a href="http://www.last.fm/api/authspec">authspec</a><br/>
   * 4.3 Create Web Service Session
   * @param token
   * @return {@link LastfmUser}
   * @throws WebPageException
   */
  public static LastfmUser createWebServiceSession(String token) {
        
    Map<String, String> params = new HashMap<String, String>();
    params.put("api_key", API_KEY);
    params.put("method", AUTH_GETSESSION);
    params.put("token", token);
    generateMethodSignature(params);
    
    XmlTag result = callLastfmMethod(params);
    
    if (genericIsLastfmError(result)) {
      return null;
    }
    
    XmlTag session = result.getChildByName("session");
    
    String username = session.getChildByNameValue("name");
    String sessionKey = session.getChildByNameValue("key");
    int subscriberInt = Integer.parseInt(session.getChildByNameValue("subscriber"));
    
    LastfmUser user = new LastfmUser(username, sessionKey, subscriberInt != 0 ? true : false);
    return user;
  }
  
  /**
   * Tune to a last.fm station
   * @param sessionKey
   * @param stationUrl url in format that begins with a "lastfm://"
   * @return new Station object on success or null if unsuccessful
   */
  public static Station tuneIn(String stationUrl) {
    
    Map<String, String> params = new HashMap<String, String>();
    params.put("api_key", API_KEY);
    params.put("method", RADIO_TUNE);
    params.put("station", stationUrl);
    params.put("sk", DataManager.getSessionKey());
    generateMethodSignature(params);
    
    XmlTag result = callLastfmMethodPost(params);
    
    
    if (genericIsLastfmError(result)) {
      return null;
    }
    
    Station lastfmStation = null;
    XmlTag station = result.getChildByName("station");
    
    String type = station.getChildByNameValue("type");
    String name = station.getChildByNameValue("name");
    String url = station.getChildByNameValue("url");
    boolean supportsDiscovery = false;
    try {
      int supportsDiscoveryInt = Integer.parseInt(
          station.getChildByNameValue("supportsdiscovery"));
      supportsDiscovery = supportsDiscoveryInt != 0 ? true : false;
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
    }
    
    lastfmStation = new Station(stationUrl, type, name, url, supportsDiscovery);
    
    return lastfmStation;
  }
  
  /**
   * Creates a new Playlist object form data fetched from Last.fm. 
   * @param sessionKey
   * @return
   */
  public static Playlist fetchPlaylist(String bitrate) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("api_key", API_KEY);
    params.put("method", RADIO_GETPLAYLIST);
    params.put("sk", DataManager.getSessionKey());
    params.put("bitrate", bitrate);
    generateMethodSignature(params);
    
    XmlTag result = callLastfmMethod(params);
    
    if (genericIsLastfmError(result)) {
      return null;
    }
    
    Playlist playlist = LastfmXmlUtil.getPlaylistFromXml(result.getChildByName("playlist"));
    
    return playlist;
  }
  
  /**
   * Attempts to scrobble the track.
   * @param track
   * @param sessionKey
   * @return true if scrobbled successfuly, false if unsuccessful
   */
  public static boolean trackScrobble(RadioTrack track) {
    if (track == null) {
      return false;
    }
    Map<String, String> params = new HashMap<String, String>();
    params.put("api_key", API_KEY);
    params.put("method", TRACK_SCROBBLE);
    params.put("sk", DataManager.getSessionKey()); 
    params.put("artist", track.getCreator());
    params.put("track", track.getTitle());
    params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
    params.put("album", track.getAlbum());
    params.put("duration", String.valueOf(track.getDuration() / 1000));
    params.put("chosenByUser", "0");
    generateMethodSignature(params);
    
    XmlTag result = callLastfmMethodPost(params);
    
    if (genericIsLastfmError(result)) {
      return false;
    }
    else {
      Log.i(TAG + "#trackScrobble(Track)", "Scrobbled successfully!");
      return true;
    }
    
  }
  
  /**
   * Updates the Now Playing notification on Last.fm profile
   * @param track
   * @param sessionKey
   * @return true if successful, false if unsuccessful
   */
  public static boolean trackUpdateNowPlaying(RadioTrack track) {
    if (track == null) {
      return false;
    }
    Map<String, String> params = new HashMap<String, String>();
    params.put("api_key", API_KEY);
    params.put("method", TRACK_UPDATE_NOWPLAYING);
    params.put("sk", DataManager.getSessionKey()); 
    params.put("artist", track.getCreator());
    params.put("track", track.getTitle());
    params.put("album", track.getAlbum());
    params.put("duration", String.valueOf(track.getDuration() / 1000));
    generateMethodSignature(params);
        
    XmlTag result = callLastfmMethodPost(params);
    
    if (genericIsLastfmError(result)) {
      mAppContext.getConsole().log(
          "Now playing notification update failed! Track=" + track.getTitle());
      return false;
    }
    else {
      mAppContext.getConsole().log(
          "Now playing notification update posted successfully! Track=" + track.getTitle());
      return true;
    }
  }
  
  public enum LoveTrackAction {
    LOVE, UNLOVE, BAN, UNBAN
  }
  
  public static int trackLove(RadioTrack track, LoveTrackAction action) {
    if (track == null) {
      return R.string.error_track_is_null;
    }
    if (action == null) {
      return R.string.error_unknown_error;
    }
    
    Map<String, String> params = new HashMap<String, String>();
    params.put("api_key", API_KEY);
    params.put("sk", DataManager.getSessionKey());
    params.put("track", track.getTitle());
    params.put("artist", track.getCreator());
    String method = null;
    int successStringCode = 0;
    switch(action) {
      case LOVE:
        method="track.love";
        successStringCode = R.string.success_love;
        break;
      case UNLOVE:
        method="track.unlove";
        successStringCode = R.string.success_unlove;
        break;
      case BAN:
        method="track.ban";
        successStringCode = R.string.success_ban;
        break;
      case UNBAN:
        method="track.unban";
        successStringCode = R.string.success_unban;
        break;
    }
    params.put("method", method);
    generateMethodSignature(params);
    
    XmlTag result = callLastfmMethodPost(params);
    
    if (genericIsLastfmError(result)) {
      return R.string.error_failed_to_love_track;
    }
    
    return successStringCode;
  }
  
  public static ArtistDetailed artistGetInfo(String artist) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("artist", artist);
    params.put("api_key", API_KEY);
    params.put("method", ARTIST_GETINFO);
    
    XmlTag result = callLastfmMethod(params);
    
    if (genericIsLastfmError(result)) {
      return null;
    }
    
    ArtistDetailed artistDetail = LastfmXmlUtil.getArtistInfoFromXml(result.getChildByName("artist"));
    return artistDetail;
  }
  
  public static Tag tagGetInfo(String tag) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("tag", tag);
    params.put("api_key", API_KEY);
    params.put("method", TAG_GETINFO);
    
    XmlTag result = callLastfmMethod(params);
    
    if (genericIsLastfmError(result)) {
      return null;
    }
    
    Tag tagInfo = LastfmXmlUtil.getTagFromXml(result.getChildByName("tag"));
    
    if (tagInfo != null) {
      tagInfo.setTags(tagGetSimilar(tag));
      tagInfo.setArtists(tagGetTopArtists(tag, 10));
    }
    
    return tagInfo;
  }
  
  public static List<Artist> tagGetTopArtists(String tag, int limit) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("tag", tag);
    params.put("api_key", API_KEY);
    params.put("method", TAG_GETTOP_ARTISTS);
    params.put("limit", String.valueOf(limit));
    
    XmlTag result = callLastfmMethod(params);
    
    if (genericIsLastfmError(result)) {
      return null;
    }
    
    List<Artist> artists = 
        LastfmXmlUtil.getSimilarArtistsFromXml(result.getChildByName("topartists"));
    
    return artists;
  }
  
  public static List<Tag> tagGetSimilar(String tag) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("tag", tag);
    params.put("api_key", API_KEY);
    params.put("method", TAG_GETSIMILAR);
    
    XmlTag result = callLastfmMethod(params);
    
    if (genericIsLastfmError(result)) {
      return null;
    }
    
    List<Tag> tags = LastfmXmlUtil.getTagsFromXml(result.getChildByName("similartags"));
    
    return tags;
  }
  
  public static Track trackGetInfo(String artistName, String trackName) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("artist", artistName);
    params.put("track", trackName);
    params.put("api_key", API_KEY);
    params.put("method", TRACK_GETINFO);
    
    XmlTag result = callLastfmMethod(params);
    
    if (genericIsLastfmError(result)) {
      return null;
    }
    
    Track track = LastfmXmlUtil.getTrackFromXml(result.getChildByName("track"));
    
    return track;
  }
  
  public static Album albumGetInfo(String artistName, String albumName) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("artist", artistName);
    params.put("album", albumName);
    params.put("api_key", API_KEY);
    params.put("method", ALBUM_GETINFO);
    
    XmlTag result = callLastfmMethod(params);
    
    if (genericIsLastfmError(result)) {
      return null;
    }
    
    Album album = LastfmXmlUtil.getAlbumFromXml(result.getChildByName("album"));
    
    return album;
  }
  
}
