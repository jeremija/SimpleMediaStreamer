package hr.jsteiner.simplemediastreamer.domain.lastfminfo;

public abstract class AbstractInfoWithImages extends AbstractInfo {
  
  protected String mImageSmall;
  protected String mImageMedium;
  protected String mImageLarge;
  protected String mImageXLarge;
  protected String mImageMega;
  
  public AbstractInfoWithImages(String name, String url) {
    super(name, url);
    // TODO Auto-generated constructor stub
  }
  
  public void setImages(String imageSmall, String imageMedium, String imageLarge, 
      String imageXLarge, String imageMega) 
  {
    mImageSmall = imageSmall;
    mImageMedium = imageMedium;
    mImageLarge = imageLarge;
    mImageXLarge = imageXLarge;
    mImageMega = imageMega;
  }

  public String getImageSmall() {
    return mImageSmall;
  }

  public void setImageSmall(String imageSmall) {
    mImageSmall = imageSmall;
  }

  public String getImageMedium() {
    return mImageMedium;
  }

  public void setImageMedium(String imageMedium) {
    mImageMedium = imageMedium;
  }

  public String getImageLarge() {
    return mImageLarge;
  }

  public void setImageLarge(String imageLarge) {
    mImageLarge = imageLarge;
  }

  public String getImageXLarge() {
    return mImageXLarge;
  }

  public void setImageXLarge(String imageXLarge) {
    mImageXLarge = imageXLarge;
  }

  public String getImageMega() {
    return mImageMega;
  }

  public void setImageMega(String imageMega) {
    mImageMega = imageMega;
  }
  
  

}
