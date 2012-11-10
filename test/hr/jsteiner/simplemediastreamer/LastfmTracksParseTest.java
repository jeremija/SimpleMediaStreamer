package hr.jsteiner.simplemediastreamer;

import hr.jsteiner.common.domain.xml.XmlTag;
import hr.jsteiner.common.util.StringUtil;
import hr.jsteiner.common.util.XmlUtil;
import hr.jsteiner.simplemediastreamer.domain.Playlist;
import hr.jsteiner.simplemediastreamer.domain.RadioTrack;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.ArtistDetailed;
import hr.jsteiner.simplemediastreamer.managers.LastfmManager;
import hr.jsteiner.simplemediastreamer.util.LastfmXmlUtil;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import android.text.Html;

public class LastfmTracksParseTest extends TestCase {
  
  public static final String xml = 
 "<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">" + 
 "<title>+Cher+Similar+Artists</title>" + 
 "<creator>Last.fm</creator>" + 
 "<date>2007-11-26T17:34:38</date>" + 
 "<link rel=\"http://www.last.fm/expiry\">3600</link>" + 
 "<trackList>" +
 " <track>" + 
 "  <location>http://play.last.fm/....mp3</location>" + 
 "  <title>TRACK1 Two People (Live)</title>" + 
 "  <identifier>8212510</identifier>" + 
 "  <album>Tina Live In Europe</album>" + 
 "  <creator>Tina Turner</creator>" + 
 "  <duration>265000</duration>" + 
 "  <image>http://images.amazon.com/images/...</image>" + 
 "  <extension application=\"http://www.last.fm/\">" + 
 "    <trackauth>12345</trackauth>" + 
 "    <artistpage>http://www.last.fm/music/Tina+Turner</artistpage>" + 
 "    <albumpage>http://www.last.fm/music/...</albumpage>" + 
 "    <trackpage>http://www.last.fm/music/...</trackpage>" + 
 "    <buyTrackURL>...</buyTrackURL>" + 
 "    <buyAlbumURL/>" + 
 "    <freeTrackURL/>" + 
 "  </extension>" + 
 " </track>" + 
 " <track>" + 
 "  <location>http://play.last.fm/....mp3</location>" + 
 "  <title>TRACK2 Two People (Live)</title>" + 
 "  <identifier>8212510</identifier>" + 
 "  <album>Tina Live In Europe</album>" + 
 "  <creator>Tina Turner</creator>" + 
 "  <duration>265000</duration>" + 
 "  <image>http://images.amazon.com/images/...</image>" + 
 "  <extension application=\"test123\">" + 
 "    <trackauth>12345</trackauth>" + 
 "    <artistpage>http://www.last.fm/music/Tina+Turner</artistpage>" + 
 "    <albumpage>http://www.last.fm/music/...</albumpage>" + 
 "    <trackpage>http://www.last.fm/music/...</trackpage>" + 
 "    <buyTrackURL>...</buyTrackURL>" + 
 "    <buyAlbumURL/>" + 
 "    <freeTrackURL/>" + 
 "  </extension>" + 
 " </track>" + 
 "</trackList>"+
 "</playlist>";
  
  public static final String error500 = 
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
      "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
      " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
      "<html>\n" +
      "  <head>\n" +
      "    <title>500 Internal Server Error</title>\n" +
      "  </head>\n" +
      "  <body>\n" +
      "    <h1>Error 500 Internal Server Error</h1>\n" +
      "    <p>Internal Serv<p>aaaa</p>er Error</p>\n" +
      "    <h3>Guru Meditation:</h3>\n" +
      "    <p>XID: 1854207447</p>\n" +
      "    <hr>\n" +
      "    <p>Varnish cache server</p>\n" +
      "  </body>\n" +
      "</html>";


  
  public static final String tuneXml =
   "<lfm status=\"ok\">" +
   " <station>" +
   "   <type>artist</type>" +
   "   <name>Cher Similar Artists</name>" +
   "   <url>http://www.last.fm/listen/artist/Cher/similarartists</url>" +
   "   <supportsdiscovery>1</supportsdiscovery>" +
   "  </station>" +
   "</lfm>";

  protected void setUp() throws Exception {
    super.setUp();
  }
  
  public void testParseXml() {
//    Playlist radioPlaylist = LastfmXmlUtil.getPlaylistFromXml(xml);
//    
//    for(Track t : radioPlaylist.getTracks()) {
//      System.out.println(t.toString());
//    }
//    
  }
  
  public void testParseTuneXml() {
    String status = StringUtil.getValueInsideXmlTags(tuneXml, "<lfm status=\"", "\">");
    System.out.println(status);
  }
  
  public void testAccessPageProvokeError() {
//    String pageContent = WebUtil.downloadPageToString("" +
//        "http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=generate-your-own");
    String pageContent = 
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
        "<lfm status=\"ok\">" +
        "<token>a-token-key</token></lfm>";
    System.out.println(pageContent);
//    String errorContent = WebUtil.downloadPageToString("" +
//        "http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=no");
    String errorContent =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
        "<lfm status=\"failed\">" + 
        "<error code=\"10\">Invalid API key - You must be granted a valid key by last.fm</error></lfm>";
    System.out.println(errorContent);
    
    String lfmStatusXml = StringUtil.regexExtract(errorContent, "<lfm.*>.*</lfm>");
    System.out.println("lfmStatusXml:   " + lfmStatusXml);
    String lfmStatusValue = StringUtil.getValueInsideXmlTags(lfmStatusXml, "<lfm status=\"", "\">");
    System.out.println("lfmStatusValue: " + lfmStatusValue);
    
    String errorXml = StringUtil.regexExtract(lfmStatusXml, "<error.*</error>");
    System.out.println("errorXml:       " + errorXml);
    
    String errorCode = StringUtil.getValueInsideXmlTags(errorXml, "<error code=\"", "\">");
    System.out.println("errorCode:      " + errorCode);
    
    String errorMessage = StringUtil.getValueInsideXmlTags(errorXml, ">", "<");
    System.out.println("errorMessage:   " + errorMessage);
  }
  
  public void testTime() {
    System.out.println(StringUtil.milisecondsToMinutesAndSeconds(1000*35 + 1000*60*35 + 1000*60*60*10));
  }
  
  public void testFindTrackInPlaylist() {
    Playlist playlist = new Playlist("naslov", "lastfm", new Date(), 3600);
    playlist.addTrack(new RadioTrack("loc3", "title", "ident", "album", "creator", 3, "image"));
    playlist.addTrack(new RadioTrack("loc2", "title", "ident", "album", "creator", 4, "image"));
    playlist.addTrack(new RadioTrack("loc3", "title", "ident", "album", "creator", 3, "image"));
    
    RadioTrack track = new RadioTrack("loc3", "title", "ident", "album", "creator", 3, "image");
    
    assertTrue(playlist.getTracks().contains(track));
    System.out.println(playlist.findTrackIndex(track));
    System.out.println(playlist.findTrackIndex(null));
  }
  
  public void testParseDate() {
    String textDate = "2012-08-05T09:36:38";
    Date playlistDate = LastfmXmlUtil.parseLastfmDate(textDate);
    System.out.println(textDate.toString());
    System.out.println(playlistDate.toString());
    
    String title = null;
    title = Html.fromHtml("track &amp;").toString();
    
    System.out.println(title);
  }
  
  public void testParseDateLong() {
    String textDate = "Sat, 17 Sep 2011 18:21:38 +0000";
    Date date = LastfmXmlUtil.parseLastfmDateLong(textDate);
    System.out.println(textDate);
    System.out.println(date.toString());
  }
  
  public void testArtistGetInfo() {
    ArtistDetailed artistDetail = LastfmManager.artistGetInfo("Deep purple");
    System.out.println(artistDetail.toString());
  }
  
  public void testParseLastfmUrl() {
    String ret = LastfmXmlUtil.parseLastfmUrl("http://www.last.fm/music/Tortapap%C3%ADr/Besh+O+Drom/_/Tortapap%C3%ADr");
    System.out.println(ret);
  }
  
  public void testParseXmlNew() {
    XmlTag parsedXml =  XmlUtil.parseXml(error500);
    System.out.println();
    //System.out.println(parsedXml);
    System.out.println();
    
    
    //XmlTag playlist = parsedXml.getNextChildInOrder();
    System.out.println(parsedXml.toXml());
    
    
//    xmlList = XmlUtil.extractSecondLevel(xmlList, "trackList");
//    System.out.println();
//    System.out.println(xmlList.toString());
//    System.out.println();
//    
//    List<XmlTag> track1 = XmlUtil.extractSecondLevel(xmlList, "track");
//    System.out.println();
//    System.out.println(track1);
//    System.out.println();
//    
//    List<XmlTag> track2 = XmlUtil.extractSecondLevel(xmlList, "track");
//    System.out.println();
//    System.out.println(track2);
//    System.out.println();
//    
//    List<XmlTag> track3 = XmlUtil.extractSecondLevel(xmlList, "track");
//    System.out.println();
//    System.out.println(track3);
  }
  
  public void testParseLastfmUrl2() {
    final int LEVELS = 4;
    
//    String url1 = "http://www.last.fm/music/Eric+Clapton";
    String url2 = "http://www.last.fm/music/Eric+Clapton/_/Cocaine";
//    String url3 = "http://www.last.fm/music/Eric+Clapton/The+Cream+Of+Clapton/Cocaine";
//    String url4 = "http://www.last.fm/music/Eric+Clapton/The+Cream+Of+Clapton";
    
    String url = url2;
    
    String path = url.replaceAll(".*?www.last.fm", "");
    System.out.println(path);
    
    Pattern p = Pattern.compile("(?<=/)[^/]*");
    Matcher m = p.matcher(path);
    
    String[] level = new String[LEVELS];
    
    for (int i = 0; i < LEVELS && m.find(); i++) {
      level[i] = m.group();
    }
    
    String level0 = level[0];
    
    if (level0 == null) {
      /*
       * can't handle this url
       */
    }
    
    if ("music".equals(level0)) {
      /*
       * do stuff for music
       */
      String artist = level[1];
      String album = level[2];
      String track = level[3];
      
      if (artist == null) {
        /*
         * can't handle this url
         */
      }
      
      if (album == null) {
        /*
         * redirect to artist activity
         */
      }
      
      if (track == null) {
        /*
         * redirect to album activity
         */
      }
      
      /*
       * redirect to track activity
       */
    }
    
    if ("tag".equals(level0)) {
      /*
       * call activity for tags
       */
    }
    
  }
  
  public void testFindElementByName() {
    XmlTag xmlTag = XmlUtil.parseXml(xml);
    XmlTag found = XmlUtil.findByName(xmlTag, "extension", "application", "test123");
    if (found != null) {
      System.out.println(found.toXml());
    }
  }
  
}
