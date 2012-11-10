package hr.jsteiner.simplemediastreamer.async;

import hr.jsteiner.simplemediastreamer.domain.lastfminfo.AbstractInfo;

/**
 * Params length must be == 1 or throws new IllegalArgumentException. The only required parameter is
 * String (name of the artist)
 * @author jere
 *
 */
public class InfoTask<Info extends AbstractInfo> 
    extends AsyncTaskCallback<String, Integer, Info>
{
  
  public LastfmManagerInfoMethodToCall<Info> mMethod = null;
  
  /**
   * 
   * @param method
   * @throws IllegalArgumentException
   */
  public InfoTask(LastfmManagerInfoMethodToCall<Info> method) {
    if (method == null) {
      throw new IllegalArgumentException("method must not be null");
    }
    mMethod = method;
  }

  @Override
  protected Info doInBackground(String... params) {
    
//    Info detail = (Info) LastfmManager.artistGetInfo(info);
    Info detail = (Info) mMethod.callLastfmMethod(params);
    
    return detail;
  }
  
  public interface LastfmManagerInfoMethodToCall<Info extends AbstractInfo> {
    public Info callLastfmMethod(String... info);
  }
  
}
