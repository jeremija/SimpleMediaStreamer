package hr.jsteiner.simplemediastreamer.async;

import android.os.AsyncTask;

/**
 * Extended {@link AsyncTask} class with a callback interface {@link OnTaskFinishListener}. 
 * @author jere
 *
 * @param <Params> Parameters to pass from {@link #execute(Object...)} to 
 * {@link #doInBackground(Object...)} method.
 * @param <Progress> Type for displaying progress.
 * @param <Result> Return type of {@link #doInBackground(Object...)} and for displaying
 * results.
 */
public abstract class AsyncTaskCallback<Params, Progress, Result> extends
  AsyncTask<Params, Progress, Result>
{
  protected OnTaskFinishListener<Result> mOnTaskFinishListener;
  
  public void setOnTaskFinishListener(OnTaskFinishListener<Result> listener) {
    mOnTaskFinishListener = listener;
  }
  
  @Override
  protected void onPostExecute(Result result) {
    if (mOnTaskFinishListener != null) {
      mOnTaskFinishListener.onTaskFinish(result);
    }
  }

  /**
   * Callback interface for {@link AsyncTaskCallback}
   * @author jere
   *
   * @param <Result>
   */
  public interface OnTaskFinishListener<Result> {
    public void onTaskFinish(Result result);
  }
  
}
