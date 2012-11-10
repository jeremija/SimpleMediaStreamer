package hr.jsteiner.simplemediastreamer.domain;

public class Station {
  private String mLastfmUrl;
  private String mType;
  private String mName;
  private String mUrl;
  private boolean mSupportsDiscovery;
  
  public Station(String lastfmUrl, String type, String name, String url, 
      boolean supportsDiscovery) 
  {
    mLastfmUrl = lastfmUrl;
    mType = type;
    mName = name;
    mUrl = url;
    mSupportsDiscovery = supportsDiscovery;
  }
  
  public String getLastfmUrl() {
    return mLastfmUrl;
  }
  public String getType() {
    return mType;
  }
  public String getName() {
    return mName;
  }
  public String getUrl() {
    return mUrl;
  }
  public boolean getSupportsDiscovery() {
    return mSupportsDiscovery;
  }

  @Override
  public String toString() {
    return "Station [mType=" + mType + ", mName=" + mName + ", mUrl=" + mUrl
        + ", mSupportsDiscovery=" + mSupportsDiscovery + "]";
  }
  
  
  
}
