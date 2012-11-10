package hr.jsteiner.simplemediastreamer.adapters;

import hr.jsteiner.simplemediastreamer.R;
import hr.jsteiner.simplemediastreamer.domain.RadioTrack;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PlaylistAdapter  extends ArrayAdapter<RadioTrack> {

  private Context mmContext = null;
  private int mCurrentTrackIndex = -1;
  
  public PlaylistAdapter(Context context, List<RadioTrack> objects,
      int currentTrackIndex) {
    super(context, 0, objects);
    mmContext = context;
    mCurrentTrackIndex = currentTrackIndex;
    
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    //return super.getView(position, convertView, parent);
    LayoutInflater inflater = (LayoutInflater)mmContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    
    View trackLayout = inflater.inflate(R.layout.playlist_item_layout, parent, false);
    TextView trackTitle = (TextView)trackLayout.findViewById(R.id.trackTitle);
    TextView trackAuthor = (TextView)trackLayout.findViewById(R.id.trackAuthor);
    TextView trackUrl = (TextView)trackLayout.findViewById(R.id.trackUrl);
    ImageView nowPlaying = (ImageView) trackLayout.findViewById(R.id.imageNowPlaying);
    
    if (position == mCurrentTrackIndex) {
      trackTitle.setTypeface(null, Typeface.BOLD);
      trackAuthor.setTypeface(null, Typeface.BOLD);
      nowPlaying.setVisibility(View.VISIBLE);
    }
    
    RadioTrack track = getItem(position);
    trackTitle.setText(track.getTitle());
    trackUrl.setText(track.getLocation());
    trackAuthor.setText(track.getCreator());
    
    return trackLayout;
  }
  
 }
