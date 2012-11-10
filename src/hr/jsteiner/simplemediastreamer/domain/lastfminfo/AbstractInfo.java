package hr.jsteiner.simplemediastreamer.domain.lastfminfo;

import java.util.Date;
import java.util.List;

public abstract class AbstractInfo {

  protected String mName;
  protected String mUrl;
  
  protected boolean mStreamable;
  protected Date mPublished;
  protected String mSummary;
  protected String mContent;
  List<Tag> mTags = null;
  
  public AbstractInfo(String name, String url) {
    mName = name;
    mUrl = url;
  }
  
  public void setTextInfo(Date published, String summary, String content) {
    mPublished = published;
    mSummary = summary;
    mContent = content;
  }
  
  public String getName() {
    return mName;
  }
  public void setName(String name) {
    mName = name;
  }
  public String getUrl() {
    return mUrl;
  }
  public void setUrl(String url) {
    mUrl = url;
  }

  public boolean isStreamable() {
    return mStreamable;
  }

  public void setStreamable(boolean streamable) {
    mStreamable = streamable;
  }

  public Date getPublished() {
    return mPublished;
  }

  public void setPublished(Date published) {
    mPublished = published;
  }

  public String getSummary() {
    return mSummary;
  }

  public void setSummary(String summary) {
    mSummary = summary;
  }

  public String getContent() {
    return mContent;
  }

  public void setContent(String content) {
    mContent = content;
  }

  public List<Tag> getTags() {
    return mTags;
  }

  public void setTags(List<Tag> tags) {
    mTags = tags;
  }
  
  

}
