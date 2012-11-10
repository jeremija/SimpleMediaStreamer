package hr.jsteiner.simplemediastreamer;

import hr.jsteiner.common.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import junit.framework.TestCase;

public class LastfmAuthTest extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }
  
  private static final char[] hexChars =
    { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  private static String toHexString(byte[] bytearray) {
      StringBuilder sb = new StringBuilder(); 
      for( byte b: bytearray ) {
              sb.append( hexChars[ b >> 4 & 0x0F ] );
              sb.append( hexChars[ b & 0x0F ] );
      }
      return sb.toString();
  }
  
  private static MessageDigest md = null;
  
  private static byte[] hash(byte[] dataToHash){
      if( md == null )
              try{
                      md = MessageDigest.getInstance("MD5");
              }catch( NoSuchAlgorithmException ignorada ){
              }
      return md.digest(dataToHash); 
  }
  
  private static String hash(String stringToHash){
      return toHexString( hash(stringToHash.getBytes()) );
  }

  
  public void testGenerateMd5FormString() {
    System.out.println(hash("password"));
  }
  
  private String returnStringFromPage(String site) {
    StringBuffer pageContentBuffer = new StringBuffer();
    try {
        
        // Create a URL for the desired page
        URL url = new URL(site);
  
        // Read all the text returned by the server
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String str;
        while ((str = in.readLine()) != null) {
            // str is one line of text; readLine() strips the newline character(s)
          pageContentBuffer.append(str);
          
        }
        in.close();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return pageContentBuffer.toString();
  }
  
  
  
  
  private String myKey = "generate-your-own";
  private String myToken = "";
  
  
  /**
   * Last.fm Authentication API - http://www.last.fm/api/authspec
   * 4.1 Fetch a request token
   */
  public void testFetchRequestToken() {
    String pageContent = returnStringFromPage("http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=" + myKey);

    System.out.println("pageContent=" + pageContent);
    myToken = StringUtil.regexExtract(pageContent, "(?<=<token>)(.*?)(?=</token>)");
    System.out.println("testToken=" + myToken + "\n");
  }
  
  /*
   * 4.2 request authorization from the user
   */
  public void testRequestAuthorizationFromUser() {
    System.out.println("\n\nGO TO URL:");
    System.out.println("http://www.last.fm/api/auth/?api_key=" + myKey + "&token=" + myToken);
  }
  
  /**
   * 4.3 Create a Web Service Session
   */
  public void testCreateWebSericeSession() {
    myToken = "0037fc5663ea0ba55f531f4584ed2623";
    
    String mySecret = "generate-your-own";
    
    String apiSignature = "api_key" + myKey + "methodauth.getSessiontoken" + myToken + mySecret;
    
    System.out.println("apiSignature=" + apiSignature);
    System.out.println("apiSignatureHash=" + hash(apiSignature));
    
    String getSessionUrl = "http://ws.audioscrobbler.com/2.0/?method=auth.getSession&api_key="
        + myKey + "&token=" + myToken + "&api_sig=" + hash(apiSignature);
    System.out.println("nextUrl=" + getSessionUrl);
        
    
    String pageContent = returnStringFromPage(getSessionUrl);
    System.out.println("\n" + pageContent);
  }
  
  public void testXmlParse() {
    String xml =  "<?xml version=\"1.0\" encoding=\"utf-8\"?><lfm status=\"ok\"><token>2b97aaa7adbd5c23443784462991171a</token></lfm>";
    String token = StringUtil.getValueInsideXmlTags(xml, "<token>", "</token>");
    System.out.println("token=" + token);
  }

}
