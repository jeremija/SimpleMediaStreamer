package hr.jsteiner.simplemediastreamer.domain;

/**
 * Contains information about lastfm errors in xml result
 * @author jsteiner
 *
 */
public class LastfmError {
  private String mStatus;
  
  private int mErrorCode;
  private String mErrorMessage;
  
  public LastfmError(String status, int errorCode, String errorMessage) {
    mStatus = status;
    mErrorCode = errorCode;
    mErrorMessage = errorMessage;
  }

  public LastfmError(String status, String errorCodeString, String errorMessage) {
    mStatus = status;
    int errorCode = 0;
    try {
      errorCode = Integer.parseInt(errorCodeString) ;
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
    }
    
    mErrorCode = errorCode;
    mErrorMessage = errorMessage;
  }

  public int getErrorCode() {
    return mErrorCode;
  }

  public String getErrorMessage() {
    return mErrorMessage;
  }

  public String getStatus() {
    return mStatus;
  }

  @Override
  public String toString() {
    return "LastfmError [mStatus=" + mStatus + ", mErrorCode=" + mErrorCode
        + ", mErrorMessage=" + mErrorMessage + "]";
  }
  
  

}
