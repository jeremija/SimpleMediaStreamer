package hr.jsteiner.simplemediastreamer.lastfm;

import hr.jsteiner.simplemediastreamer.ApplicationEx;
import hr.jsteiner.simplemediastreamer.R;
import hr.jsteiner.simplemediastreamer.StreamerActivity;
import hr.jsteiner.simplemediastreamer.async.AsyncTaskCallback.OnTaskFinishListener;
import hr.jsteiner.simplemediastreamer.async.AsyncTaskCallbackWrapper;
import hr.jsteiner.simplemediastreamer.async.AsyncTaskCallbackWrapper.BackgroundTask;
import hr.jsteiner.simplemediastreamer.async.DownloadImagesTask;
import hr.jsteiner.simplemediastreamer.async.InfoTask;
import hr.jsteiner.simplemediastreamer.async.InfoTask.LastfmManagerInfoMethodToCall;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.AbstractInfo;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.AbstractInfoWithImages;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Artist;
import hr.jsteiner.simplemediastreamer.domain.lastfminfo.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class LastfmInfoActivityAbstract<Info extends AbstractInfo> extends Activity
implements OnTaskFinishListener<Info>, BackgroundTask<Info> {
  protected static final String SIMILAR_KEY = "::SIMILAR::";
  
  public static final String TAG = LastfmInfoActivityAbstract.class.getCanonicalName();
  
  public static final String LASTFM_URL = "http://www.last.fm/";
  
  public static final String INFO_TYPE_TAG = "tag";
  public static final String INFO_TYPE_ARTIST = "music";
  
  public static final String RADIO_URL_START = "lastfm://";
  
  private String mRadioUrl = null;;
  private String mRadioUrlAppendix = null;
  private boolean mRadioUrlSet = false;
  
  protected String mTypeTitleText = null;
  
  protected ApplicationEx getAppContext() {
    return (ApplicationEx) getApplicationContext();
  }
  
  /**
   * TASKS TO CANCEL
   */
  
  @SuppressWarnings("rawtypes")
  protected List<AsyncTask> mTasks = new ArrayList<AsyncTask>();
  private AsyncTaskCallbackWrapper<Info> mInfoTask = null;
  
  @SuppressWarnings("rawtypes")
  protected void cancelTasks() {
    if (mInfoTask != null) {
      boolean cancelled = mInfoTask.cancel(true);
      Log.d(TAG + "#cancelTasks()", "mInfoTask cancelled=" + cancelled);
    }
    for (AsyncTask task : mTasks ) {
      if (task != null) {
        boolean cancelled = task.cancel(true);
        Log.d(TAG + "#cancelTasks()", "other mTasks cancelled=" + cancelled);
      }
    }
    mTasks = new ArrayList<AsyncTask>();
    dismissProgressDialog();
  }
  
  protected void dismissProgressDialog() {
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }
  }
  
  /**
   * Cancel currently running tasks (and dismiss existing progress dialog if any) and show the 
   * progress dialog again!
   */
  protected void showProgressDialog() {
    cancelTasks();
    
    mProgressDialog = ProgressDialog.show(this, 
        getString(R.string.info_loading_title), getString(R.string.info_loading_msg));
    mProgressDialog.setCancelable(true);
    mProgressDialog.setOnCancelListener(new OnCancelListener() {
      @Override public void onCancel(DialogInterface dialog) {
        cancelTasks();
      }
    });
  }
  
  @SuppressWarnings("rawtypes")
  protected void removeFinishedTask(AsyncTask task) {
    if (mTasks.contains(task)) {
      mTasks.remove(mTasks.indexOf(task));
    }
  }
  
  protected ProgressDialog mProgressDialog = null;
  
  /**
   * DATA FOR STORAGE (ON ROTATION, ETC)
   */
  protected Map<String, Bitmap> mSimilarArtistsMap = null;
  protected Bitmap mMainImage = null;
  protected Info mInfo = null;
  
  /*
   * LAYOUT ITEMS
   */
  protected ImageView mArtistInfoImage = null;
  protected LinearLayout mArtistInfoTagsLayout = null;
  protected LinearLayout mArtistInfoSimilarLayout = null;
  protected TextView mArtistInfoBioText = null;
  protected TextView mTypeTitle = null;
  
  /**
   * Set the {@link #mRadioUrl} here to value like (artist). This will be used when 
   * the Play button from the Action Bar is clicked.
   */
  protected abstract void setRadioUrl();
  
  protected void setRadioUrl(String radioUrl, String radioUrlAppendix) {
    mRadioUrl = RADIO_URL_START + radioUrl;
    mRadioUrlAppendix = radioUrlAppendix;
    mRadioUrlSet = true;
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initState();
    initView();
    
    /*
     * this works ok, but for some reason I get log error message:
     * JavaBinder: !!! FAILED BINDER TRANSACTION !!!
     * 
     * I guess that this is due to the Bitmap image size.
     */
//    if (savedInstanceState != null) {
//      restoreStateFromBundle(savedInstanceState);
//      return;
//    }
    
    Intent intent = getIntent();
    if (intent != null) {
      handleIntent(intent);
    }
  }
  
  protected void initState() {
    setRadioUrl();
    if (mRadioUrlSet == false) {
      throw new IllegalStateException(
          "You must implement the setRadioMethod and call the setRadioUrl(String,String) from there");
    }

  }
  
  protected void initView() {
    setContentView(R.layout.lastfm_info_layout);
    mArtistInfoImage = (ImageView) findViewById(R.id.infoImage);
    mArtistInfoTagsLayout = (LinearLayout) findViewById(R.id.infoTagsLayout);
    mArtistInfoSimilarLayout = (LinearLayout) findViewById(R.id.infoSimilarArtistsLayout);
    mArtistInfoBioText = (TextView) findViewById(R.id.infoMainText);
    mTypeTitle = (TextView) findViewById(R.id.infoType);
  }
  
  protected void setTypeTitle(String typeTitle) {
    mTypeTitleText = typeTitle;
    mTypeTitle.setText(typeTitle);
  }
  
//  protected void restoreStateFromBundle(Bundle savedInstanceState) {
//    if (savedInstanceState == null) {
//      return;
//    }
//    
//    Bitmap mainImage = (Bitmap) savedInstanceState.getParcelable("mainImage");
//    setMainImage(mainImage);
//    
//    
//    @SuppressWarnings("unchecked")
//    Info info = (Info) savedInstanceState.getSerializable("info");
//    setInfo(info, false);
//    
//    Map<String, Bitmap> similarArtistsMap = new HashMap<String, Bitmap>();
//    
//    Set<String> keys = savedInstanceState.keySet();
//    Iterator<String> it = keys.iterator();
//    while (it.hasNext()) {
//      String key = it.next();
//      if (key.startsWith(SIMILAR_KEY)) {
//        String similarArtistName = key.replace(SIMILAR_KEY, "");
//        Bitmap similarArtistImage = savedInstanceState.getParcelable(key);
//        similarArtistsMap.put(similarArtistName, similarArtistImage);
//      }
//    }
//    
//    inflateSimilarArtists(similarArtistsMap);
//  }
  
//  @Override
//  protected void onSaveInstanceState(Bundle outState) {
//    outState.putParcelable("mainImage", mMainImage);
//
//    outState.putSerializable("info", mInfo);
//    
//    if (mSimilarArtistsMap != null) {
//      Iterator<Entry<String, Bitmap>> it = mSimilarArtistsMap.entrySet().iterator();
//  
//      while (it.hasNext()) {
//        Entry<String, Bitmap> entry = it.next();
//        outState.putParcelable(SIMILAR_KEY + entry.getKey(), entry.getValue());
//      }
//    }
//    
//    super.onSaveInstanceState(outState);
//  }
  
  /**
   * This will get called from {@link #onCreate(Bundle)} if Bundle is null and 
   * {@link #getIntent()} returns an Intent with action which is not null
   * @param intent
   * @param action
   */
  protected abstract void handleIntent(Intent intent);
//  protected void handleIntent(Intent intent, String action) {
//    if (intent == null) {
//      Log.e(TAG + "#handleIntent(Intent,String)", "Intent is null!");
//      return;
//    }
//    
//    if (Intent.ACTION_VIEW.equals(action)) {
//      
//      if (intent.getData() != null) {
////TODO        fetchInfo(params);
//        //String uri = intent.getData().toString();
//        //String url = LastfmXmlUtil.parseLastfmUrl(uri);
//        //fetchInfo(url);
//        //return;
//      }
//      
//      //fetchInfo(intent.getStringExtra("info"));
//    }
//  }
  
  @Override
  protected void onDestroy() {
    /** to prevent exception when loading and rotating at the same time **/
    cancelTasks();
    super.onDestroy();
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.info_menu, menu);
    addCustomItemsMenu(menu);
    return super.onCreateOptionsMenu(menu);
  }
  
  /**
   * Use this method to add custom items to the menu - useful for adding extra menu items in
   * in different actvities.<br/>
   * <br/>
   * For example:<br/>
   * <code>
   * {@link MenuItem} item = menu.add("newMenuItem);<br/>
   * <br/>
   * // set the callback function<br/>
   * item.setOnMenuItemClickListener(...); </br>
   * </code>
   * <br/>
   * If you do not wish to use additional menu items, just leave this method's body empty.
   * @param menu
   */
  public abstract void addCustomItemsMenu(Menu menu);
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case R.id.info_menu_search:
        popupSearch();
        return true;
      case R.id.info_menu_play:
        if (mRadioUrl != null) {
          String url = mRadioUrl + getTitle();
          if (mRadioUrlAppendix != null) url += mRadioUrlAppendix;
          playRadio(Uri.parse(url));
        }
        else {
          getAppContext().getConsole().toast(R.string.feature_not_available_here);
        }
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
  
  protected void popupSearch() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.info_dialog_search);
    final EditText searchInput = new EditText(this);
    builder.setView(searchInput);
    builder.setPositiveButton(R.string.info_dialog_positive, 
        new DialogInterface.OnClickListener() 
    {
      @Override public void onClick(DialogInterface dialog, int which) {
        onSearch(searchInput.getText().toString());
      }
    });
    builder.setNegativeButton(R.string.info_dialog_negative, 
        new DialogInterface.OnClickListener() 
    {
      @Override public void onClick(DialogInterface dialog, int which) {
        //do nothing
      }
    });
    
    AlertDialog dialog = builder.create();
    dialog.show();
  }
  
  /**
   * Method for fetching main Last.fm info.<br/>
   * <br/>
   * This method should be called from {@link #handleIntent(Intent)}.
   * Make sure that the number of parameters you pass to this method matches the number of 
   * parameters to the method called in your implmementation of 
   * {@link InfoTask.LastfmManagerInfoMethodToCall} which you should set in
   * {@link #setInfoMehthod(LastfmManagerInfoMethodToCall)}.
   * @param info
   */
  protected void fetchInfo() {
    cancelTasks();
    showProgressDialog();
    
    mInfoTask = new AsyncTaskCallbackWrapper<Info>(this);
    mInfoTask.setOnTaskFinishListener(this);
    mInfoTask.execute();
  }
  
  @Override public void onTaskFinish(Info info) {
    if (info == null) {
      dismissProgressDialog();
    }
    setInfo(info, true);
    mInfoTask = null;
  }

  protected void fetchImages(final String mainImageUrl, final List<Artist> similarArtists) {
    final DownloadImagesTask downloadImagesTask = new DownloadImagesTask();
    mTasks.add(downloadImagesTask);
    downloadImagesTask.setOnTaskFinishListener(new OnTaskFinishListener<List<Bitmap>>() {
      @Override public void onTaskFinish(List<Bitmap> result) {
        if (result == null) {
          return;
        }
        
        if (result.size() > 0) {
          setMainImage(result.get(0));
          result.remove(0);
        }
        
        if (similarArtists == null) {
          dismissProgressDialog();
          return;
        }
        
        Map<String, Bitmap> similarArtistsMap = new HashMap<String, Bitmap>();
        for (int i = 0; i < result.size() || i < similarArtists.size(); i ++) {
          Artist similarArtist = similarArtists.get(i);
          if (similarArtist != null) {
            similarArtistsMap.put(similarArtist.getName(), result.get(i));
          }
        }
        inflateSimilarArtists(similarArtistsMap);
        dismissProgressDialog();
        removeFinishedTask(downloadImagesTask);
      }
    });
    
    List<String> urls = new ArrayList<String>();
    urls.add(mainImageUrl);
    if (similarArtists != null) {
      for (Artist artist : similarArtists) {
        urls.add(artist.getImageLarge());
      }
    }
    downloadImagesTask.execute(urls.toArray(new String[urls.size()]));
  }
  
  /**
   * Play radio
   * @param uri to play from (for example lastfm://artist/Deep Purple/similarartists
   */
  protected void playRadio(Uri uri) {
    Intent intent = new Intent(this, StreamerActivity.class);
    intent.setAction(Intent.ACTION_VIEW);
    intent.setData(uri);
    startActivity(intent);
  }
  
  protected void setMainImage(Bitmap mainImage) {
    mMainImage = mainImage;
    mArtistInfoImage.setImageBitmap(mainImage);
  }
  
  protected void inflateSimilarArtists(Map<String, Bitmap> similarArtistsMap) {
    mSimilarArtistsMap = similarArtistsMap;
    if (similarArtistsMap == null) {
      return;
    }
    
    final Context context = this;
    
    Iterator<Entry<String, Bitmap>> it = similarArtistsMap.entrySet().iterator();
    while (it.hasNext()) {
      final Entry<String, Bitmap> entry = it.next();
      
      View view = View.inflate(this, R.layout.similar_artists, null);
      mArtistInfoSimilarLayout.addView(view);
      
      TextView similarName = (TextView) view.findViewById(R.id.similarName);
      ImageView similarImage = (ImageView) view.findViewById(R.id.similarImage);
      Button similarButton = (Button) view.findViewById(R.id.similarButton);
      
      similarName.setText(entry.getKey());
      similarImage.setImageBitmap(entry.getValue());
      similarButton.setOnClickListener(new OnClickListener() {
        @Override public void onClick(View v) {
          InfoSearchParam ifs = InfoSearchParam.generateArtistSearchParam(entry.getKey());
          if (ifs != null) ifs.startActivity(context);
        }
      });
    }
  }
  
  protected void inflateTags(List<Tag> tags) {
    if (tags == null) {
      return;
    }
    
    final Context context = this;
    
    for (final Tag tag : tags) {
      Button button = (Button) View.inflate(this, R.layout.similar_tags, null);
      button.setText(tag.getName());
      button.setOnClickListener(new OnClickListener() {
        @Override public void onClick(View v) {
//          playRadio(Uri.parse("lastfm://tag/" + tag.getName()));
          //prepareIntentAndStartNewInfoActivity(context, tag.getName(), INFO_TYPE_TAG);
          InfoSearchParam ifs = InfoSearchParam.generateTagSearchParam(tag.getName());
          ifs.startActivity(context);
        }
      });
      
      mArtistInfoTagsLayout.addView(button);
    }
  }
  
  protected void setContent(String content) {
    if (content == null) {
      return;
    }
    Spanned bio = Html.fromHtml(content);
    if (bio != null) {
      /** for some reason calling setMovementMethod this way enables the hyperlinks in TextView**/
      mArtistInfoBioText.setMovementMethod(LinkMovementMethod.getInstance());
      mArtistInfoBioText.setText(bio);
    }
  }
  
//  public static void prepareIntentAndStartNewInfoActivity(
//      Context context, String info, String infoType)
//  {
//    if (info == null || infoType == null) {
//      Log.e(TAG + "#tryToFetchNextInfo(String,String)", "One of the parameters is null");
//    }
//    
//    try {
//      info = URLEncoder.encode(info, "UTF-8");
//    } catch (UnsupportedEncodingException e) {
//      e.printStackTrace();
//    }
//    
//    String url = LASTFM_URL + infoType + "/" +  info;
//    Log.d(TAG + "#prepareIntentAndStartNewInfoActivity(Context,String,String)",
//        "intent url=" + url);
//    
//    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//    //intent.putExtra("info", artist);
//    context.startActivity(intent);
//  }

  protected void onSearch(String input) {
    InfoSearchParam isp = InfoSearchParam.generateArtistSearchParam(input);
    isp.startActivity(this);
  }
  
  protected abstract void setInfo(Info info, boolean fetchImages);
  
  /**
   * Tries to find the larges image url from {@link AbstractInfoWithImages}. 
   * @param info
   * @return image url if found, null if not found.
   */
  protected String getLargestImageUrl(AbstractInfoWithImages info) {
    if (info == null) {
      return null;
    }
    
    if (info.getImageMega() != null) {
      return info.getImageMega();
    } else if (info.getImageXLarge() != null) {
      return info.getImageXLarge();
    } else if (info.getImageLarge() != null) {
      return info.getImageLarge();
    } else if (info.getImageMedium() != null) {
      return info.getImageMedium();
    } else if (info.getImageSmall() != null) {
      return info.getImageSmall();
    } else { 
      return null;
    }
  }
}
