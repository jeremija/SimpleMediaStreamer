package hr.jsteiner.simplemediastreamer.async;

import hr.jsteiner.simplemediastreamer.async.AsyncTaskCallback.OnTaskFinishListener;

/**
 * This is a wrapper class for {@link AsyncTaskCallback}. The main benefit of using this class is
 * that it explicitly enables the user to see which parameters are passed on to the methods called
 * in this task. 
 * @author jere
 *
 * @param <ReturnType>
 */
public class AsyncTaskCallbackWrapper<ReturnType> {
  
  private AsyncTaskCallback<String, Integer, ReturnType> mTask = null;
  
  private BackgroundTask<ReturnType> mBackgroundTask = null;
      
  /**
   * 
   * @param backgroundTask task to execute
   * @param listener callback interface which will be called after the task is completed
   */
  public AsyncTaskCallbackWrapper(BackgroundTask<ReturnType> backgroundTask,
      OnTaskFinishListener<ReturnType> listener) 
  {
    setBackgroundTask(backgroundTask);
    createTask();
    setOnTaskFinishListener(listener);
  }
  
  /**
   * @param backgroundTask task to execute
   */
  public AsyncTaskCallbackWrapper(BackgroundTask<ReturnType> backgroundTask) {
    setBackgroundTask(backgroundTask);
    createTask();
  }
  
  private void createTask() {
    mTask = new AsyncTaskCallback<String, Integer, ReturnType>() {
      @Override
      protected ReturnType doInBackground(String... parameters) {
        /*
         * The parameters array will not be used because the parameters to call will
         * be passed via the BackgroundTask interface. 
         */
        if (mBackgroundTask == null) {
          throw new IllegalStateException("mBackgroundTask must be set");
        }
        
        return mBackgroundTask.doBackgroundTask();
      }
    };
  }
  
  protected void setBackgroundTask(BackgroundTask<ReturnType> backgroundTask) {
    if (backgroundTask == null) {
      throw new IllegalStateException("backgroundTask must not be null");
    }
    mBackgroundTask = backgroundTask;
  }
  
  public void setOnTaskFinishListener(OnTaskFinishListener<ReturnType> listener) {
    mTask.setOnTaskFinishListener(listener);
  }
  
  public interface BackgroundTask<ReturnType> {
    public ReturnType doBackgroundTask();
  }
  
  public void execute() {
    mTask.execute();
  }
  
  /**
   * See {@link AsyncTaskCallback#cancel(boolean)}
   * @param mayInterruptIfRunning
   */
  public boolean cancel(boolean mayInterruptIfRunning) {
    return mTask.cancel(mayInterruptIfRunning);
  }
}
