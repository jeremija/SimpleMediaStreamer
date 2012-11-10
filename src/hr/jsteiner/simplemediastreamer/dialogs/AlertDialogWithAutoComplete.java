package hr.jsteiner.simplemediastreamer.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.AutoCompleteTextView;

public class AlertDialogWithAutoComplete extends AlertDialog{
  
  private AutoCompleteTextView mAutoCompleteTextView;

  protected AlertDialogWithAutoComplete(Context context) 
  {
    super(context);
  }
  
  /**
   * if this is set, the the showDropDown() method will be called afterwards 
   */
  public void setAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView) {
    mAutoCompleteTextView = autoCompleteTextView;
  }

  @Override
  protected void onStart() {
    if (mAutoCompleteTextView != null) {
      mAutoCompleteTextView.showDropDown();
    }
    
    super.onStart();
  }
  
  
  

}
