package com.git.programmerr47.testhflbjcrhjggkth.view.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.git.programmerr47.testhflbjcrhjggkth.R;
import com.git.programmerr47.testhflbjcrhjggkth.controllers.TestController;
import com.git.programmerr47.testhflbjcrhjggkth.model.MicroScrobblerModel;
import com.git.programmerr47.testhflbjcrhjggkth.model.database.data.SongData;
import com.git.programmerr47.testhflbjcrhjggkth.model.managers.SearchManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class TestPageFragment extends Fragment {
	static final String ARGUMENT_RADIO_ID = "arg_rad_id";
    
    TestController controller;
    MicroScrobblerModel model;
    SearchManager searchManager;
    Activity parentActivity;
    
    LinearLayout songHistory;
    TextView songArtist;
    TextView songTitle;
    TextView songDate;
    TextView status;
    ImageView songCoverArt;

    public static TestPageFragment newInstance() {
    		TestPageFragment pageFragment = new TestPageFragment();
            Bundle arguments = new Bundle();
            pageFragment.setArguments(arguments);
            return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            controller = new TestController(this);
            model = MicroScrobblerModel.getInstance();
            searchManager = model.getSearchManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_fragment, null);
        
		songHistory = (LinearLayout) view.findViewById(R.id.songHistoryItem);
		songHistory.setVisibility(View.GONE);
		songArtist = (TextView) view.findViewById(R.id.songListItemArtist);
		songTitle = (TextView) view.findViewById(R.id.songListItemTitle);
		songDate = (TextView) view.findViewById(R.id.songListItemDate);
		status = (TextView) view.findViewById(R.id.status);
		
		songCoverArt = (ImageView) view.findViewById(R.id.songListItemCoverArt);
		
		final EditText artistEditText = (EditText) view.findViewById(R.id.artist);
		final EditText titleEditText = (EditText) view.findViewById(R.id.title);
		final EditText albumEditText = (EditText) view.findViewById(R.id.album);
		
        Button searchButton = (Button) view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String artist = artistEditText.getText().toString();
				String title = titleEditText.getText().toString();
				String album = albumEditText.getText().toString();
				controller.search(artist, album, title);
			}
		});
        
        return view;
    }
    
    public void displayStatus(String statusString) {
    	status.setText(statusString);
    }
    
    public void displaySongInformationElement(SongData songData) {
    	if(songData != null) {
			String coverArtUrl = songData.getCoverArtURL();
			songHistory.setVisibility(View.VISIBLE);
			songArtist.setText(songData.getArtist());
			songTitle.setText(songData.getTitle());
			songDate.setText("just now");
			DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.no_cover_art)
				.showImageOnFail(R.drawable.no_cover_art)
				.build();
			model.getImageLoader().displayImage(coverArtUrl, songCoverArt, options);
		}
    }
   
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("SongPlayer", "HistoryPageFragment onDestroy()");
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	//updateRecognizeStatus();
    	//TODO теперь для onResume нужно сохранять текущую информацию в этом классе
    }
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = activity;
    }
}