package hr.jsteiner.simplemediastreamer.domain;

/**
 * LastfmUser stores the parameters like username, sessionKey and boolean which determines if the
 * user is a subscriber or not.
 * @author jere
 *
 */
public class LastfmUser {
  
  private String mUsername = null;
  private String mSessionKey = null;
  private boolean mSubscriber = false;
  
  /**
   * @param username
   * @param sessionKey
   * @param subscriber
   * @throws IllegalArgumentException if username or sessionKey is null
   */
  public LastfmUser(String username, String sessionKey, boolean subscriber) {
    if (username == null || sessionKey == null) {
      throw new IllegalArgumentException();
    }
    
    mUsername = username;
    mSessionKey = sessionKey;
    mSubscriber = subscriber; 
  }

  public String getUsername() {
    return mUsername;
  }

  public String getSessionKey() {
    return mSessionKey;
  }

  public boolean isSubscriber() {
    return mSubscriber;
  }
  
  public String toString() {
    return "username=" + mUsername + ",session=" + mSessionKey + ",subscriber=" + mSubscriber;
  }
  
}
