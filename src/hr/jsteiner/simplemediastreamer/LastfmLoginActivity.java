package hr.jsteiner.simplemediastreamer;

import hr.jsteiner.simplemediastreamer.domain.LastfmUser;
import hr.jsteiner.simplemediastreamer.managers.DataManager;
import hr.jsteiner.simplemediastreamer.managers.LastfmManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * Provides the login and logoff options to the lastfm service.
 * @author jsteiner
 *
 */
public class LastfmLoginActivity extends Activity implements OnClickListener,
OnCheckedChangeListener {
 
  public static final String TAG = LastfmLoginActivity.class.getCanonicalName();
  
  LastfmUser mUser = null;
  
  TextView mTextUsername = null;
  TextView mTextSession = null;
  TextView mTextSubscriber = null;
  CheckBox mCheckLowBitrate = null;
  Button mLoginButton = null;
  Button mLogoutButton = null;
  
  Handler mHandler = new Handler();
  Context mContext = null;
  
  private ApplicationEx getAppContext() {
    return (ApplicationEx) getApplicationContext();
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;
    
    setContentView(R.layout.lastfm_login_layout);
    initView();
    
    LastfmUser user = DataManager.getCurrentLastfmUser();
    updateUserInfo(user);
  }
  
  private void initView() {
    
    mTextUsername = (TextView) findViewById(R.id.textUsername);
    mTextSession = (TextView) findViewById(R.id.textSession);
    mTextSubscriber = (TextView) findViewById(R.id.textSubscriber);
    mCheckLowBitrate = (CheckBox) findViewById(R.id.checkLowBitrate);
    mLoginButton = (Button) findViewById(R.id.buttonLogin);
    mLogoutButton = (Button) findViewById(R.id.buttonLogout);
    
    mCheckLowBitrate.setChecked(DataManager.isLowBitrate());
    
    mCheckLowBitrate.setOnCheckedChangeListener(this);
    mLoginButton.setOnClickListener(this);
    mLogoutButton.setOnClickListener(this);
  }
  
  private void updateUserInfo(LastfmUser user) {
    mUser = user;
    
    if (mUser != null) {
      mTextUsername.setText(mUser.getUsername());
      mTextSession.setText(mUser.getSessionKey());
      mTextSubscriber.setText(mUser.isSubscriber() ? "true" : "false");
      
      mLoginButton.setEnabled(false);
    }
    else {
      mTextUsername.setText(null);
      mTextSession.setText(null);
      mTextSubscriber.setText(null);
      
      mLoginButton.setEnabled(true);
    }
  }
  
  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.buttonLogin) buttonLoginClickAction();
    if (v.getId() == R.id.buttonLogout) buttonLogoutClickAction();
  }
  
  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (buttonView.getId() == R.id.checkLowBitrate) {
      getAppContext().getConsole().log("Low bitrate: " + isChecked);
      DataManager.setLowBitrate(isChecked);
    }
  }
  
  private void buttonLoginClickAction() {
    /**
     * Needs to be an AsyncTask because it accesses the Internet.
     */
    FetchRequestTokenTask fetchRequestTokenTask = new FetchRequestTokenTask();
    fetchRequestTokenTask.execute("do it!");
  }
  
  private void afterUserAuthorized(final String token) {
    
    /*
     * Show AlertDialog to pause the execution before the user authorizes.
     */
    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
    builder.setTitle(R.string.lastfm_login_alert_title);
    builder.setMessage(R.string.lastfm_login_alert_message);
    builder.setPositiveButton(R.string.lastfm_login_alert_continue, 
        new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        /**
         * Needs to be an AsyncTask because it accesses the Internet.
         * This is executed after the user has dismissed this AlertDialog.
         */
        AfterUserAuthorizedTask afterUserAuthorizedTask = new AfterUserAuthorizedTask();
        afterUserAuthorizedTask.execute(token);
      }
    });
    
    AlertDialog alertDialog = builder.create();
    alertDialog.show();
  }
  
  private void buttonLogoutClickAction() {
    LastfmUser user = null;
    DataManager.saveCurrentLastfmUser(user);
    updateUserInfo(user);
  }
  
  /**
   * Calls the fetchRequestToken() method and after the task is completed it redirects the user to 
   * last.fm page to authorize this application. 
   * 
   * Execute params: none used.
   * @author jsteiner
   */
  private class FetchRequestTokenTask extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... params) {
      String token = LastfmManager.fetchRequestToken();

      return token;
    }

    @Override
    protected void onPostExecute(String token) {
      if (token == null) {
        getAppContext().getConsole().toast(getString(R.string.error_authenticating_token));
        return;
      }
      /*
       * send the user to the last.fm web page to request authorization
       */
      String url = LastfmManager.requestAuthorizationFromUser(token);
      Log.d(TAG + "#onPostExecute(String)", "autorizationUrl=" + url);
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      startActivity(intent);
      
      afterUserAuthorized(token);
    }
  }
  
  /**
   * This task attempts to create a WebService session. 
   * 
   * Execute params: Only the first parameter is used and it should be the token String.
   * @author jsteiner
   *
   */
  private class AfterUserAuthorizedTask extends AsyncTask<String, Integer, LastfmUser> {

    @Override
    protected LastfmUser doInBackground(String... params) {
      if (params.length != 1) {
        throw new IllegalArgumentException("Parameter must be only one String (token)!");
      }
      String token = params[0];
      
      /*
       * after the user has clicked continue
       */
      Log.d(TAG + "#AfterUserAuthorizedTask.doInBackground(String[])", 
          "lastfm_alert_dialog_continue button clicked!");
      
      LastfmUser user = LastfmManager.createWebServiceSession(token);
      
      return user;
    }

    @Override
    protected void onPostExecute(LastfmUser user) {
      if (user == null) {
        getAppContext().getConsole().toast(getString(R.string.error_authenticating_user));
      }
      DataManager.saveCurrentLastfmUser(user);
      updateUserInfo(user);
    }
    
    
    
  }
  
}
