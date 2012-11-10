package hr.jsteiner.simplemediastreamer;

import hr.jsteiner.common.logging.Console;
import hr.jsteiner.simplemediastreamer.async.DownloadImagesTask;
import hr.jsteiner.simplemediastreamer.domain.Station;
import hr.jsteiner.simplemediastreamer.managers.DataManager;
import hr.jsteiner.simplemediastreamer.managers.LastfmManager;
import hr.jsteiner.simplemediastreamer.media.Player;
import hr.jsteiner.simplemediastreamer.util.LastfmXmlUtil;
import android.app.Application;

public class ApplicationEx extends Application{
  private Console mConsole = Console.getInstance();
  
  private Station mLastTunedStation = null;

  public Console getConsole() {
    return mConsole;
  }

  @Override
  public void onCreate() {
    /** this is set so these methods can access the Console object **/
    DownloadImagesTask.setAppContext(this);
    LastfmManager.setAppContext(this);
    LastfmXmlUtil.setAppContext(this);
    Player.setAppContext(this);
    
    /** this is set so that the context doesn't need to be passed every time **/
    DataManager.setAppContext(this);
    
    /** this is set so that the context doesn't need to be passed for every toast **/
    Console.setAppContext(this);
    
    super.onCreate();
  }
  
  public void setLastTunedStation(Station station) {
    mLastTunedStation = station;
  }
  
  public Station getLastTunedStation() {
    return mLastTunedStation;
  }
  
}
