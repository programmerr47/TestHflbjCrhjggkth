package com.git.programmerr47.testhflbjcrhjggkth.view.fragments;

import java.util.Date;

import com.git.programmerr47.testhflbjcrhjggkth.R;
import com.git.programmerr47.testhflbjcrhjggkth.controllers.RecognizeController;
import com.git.programmerr47.testhflbjcrhjggkth.model.MicroScrobblerModel;
import com.git.programmerr47.testhflbjcrhjggkth.model.SongData;
import com.git.programmerr47.testhflbjcrhjggkth.model.managers.FingerprintManager;
import com.git.programmerr47.testhflbjcrhjggkth.model.managers.RecognizeManager;
import com.git.programmerr47.testhflbjcrhjggkth.model.observers.IFingerprintStatusObserver;
import com.git.programmerr47.testhflbjcrhjggkth.model.observers.IRecognizeResultObserver;
import com.git.programmerr47.testhflbjcrhjggkth.model.observers.IRecognizeStatusObserver;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecognizePageFragment extends Fragment implements IRecognizeStatusObserver, IRecognizeResultObserver, IFingerprintStatusObserver {
	static final String ARGUMENT_RADIO_ID = "arg_rad_id";
    
    RecognizeController controller;
    MicroScrobblerModel model;
    RecognizeManager recognizeManager;
    FingerprintManager fingerprintManager;
    Activity parentActivity;
    
    LinearLayout infoDialog;
    TextView songArtist;
    TextView songTitle;
    TextView songDate;
    TextView status;
    ImageView songCoverArt;
    
    boolean firstApearing = false;
    
    public static RecognizePageFragment newInstance() {
    		RecognizePageFragment pageFragment = new RecognizePageFragment();
            Bundle arguments = new Bundle();
            pageFragment.setArguments(arguments);
            return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            controller = new RecognizeController();
            model = MicroScrobblerModel.getInstance();
            fingerprintManager = model.getFingerprintManager();
            fingerprintManager.addObserver((IFingerprintStatusObserver)this);
            recognizeManager = model.getRecognizeManager();
            recognizeManager.addObserver((IRecognizeStatusObserver)this);
            recognizeManager.addObserver((IRecognizeResultObserver)this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recognize_fragment, null);
        
		infoDialog = (LinearLayout) view.findViewById(R.id.songHistoryItem);
		songArtist = (TextView) view.findViewById(R.id.songListItemArtist);
		songTitle = (TextView) view.findViewById(R.id.songListItemTitle);
		songDate = (TextView) view.findViewById(R.id.songListItemDate);
		status = (TextView) view.findViewById(R.id.status);
		infoDialog.setVisibility(View.INVISIBLE);
		
		songCoverArt = (ImageView) view.findViewById(R.id.songListItemCoverArt);
        
        ImageButton microTimerListenButton = (ImageButton) view.findViewById(R.id.microTimerListenButton);
        microTimerListenButton.setOnLongClickListener(new View.OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				Log.v("Recognizing", "Recognize by timer: onLongClick");
				return controller.fingerprintByTimerRecognizeCancel();
			}
		});
        
        ImageButton microNowListenButton = (ImageButton) view.findViewById(R.id.microNowListenButton);
        microNowListenButton.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				Log.v("Recognizing", "Recognize now: onLongClick");
				return controller.fingerprintNowRecognizeCancel();
			}
		});
        
        return view;
    }
   
    @Override
    public void onDestroy() {
        super.onDestroy();
        fingerprintManager.removeObserver(this);
        recognizeManager.removeObserver((IRecognizeStatusObserver)this);
        recognizeManager.removeObserver((IRecognizeResultObserver)this);
        Log.v("SongPlayer", "HistoryPageFragment onDestroy()");
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	//updateFingerprintStatus();
    	//updateRecognizeStatus();
    	//TODO теперь нужно хранить внутри класса текущую информацию для onResume
    }
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = activity;
    }

	@Override
	public void onFingerprintStatusChanged(String status) {
		this.status.setText(status);
	}

	@Override
	public void onRecognizeResult(SongData songData) {
		if(songData != null) {
			String coverArtUrl = songData.getCoverArtUrl();
			infoDialog.setVisibility(View.VISIBLE);
			songArtist.setText(songData.getArtist());
			songTitle.setText(songData.getTitle());
			songDate.setText(songData.getDate().toString());
			DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.no_cover_art)
				.showImageOnFail(R.drawable.no_cover_art)
				.build();
			model.getImageLoader().displayImage(coverArtUrl, songCoverArt, options);
		}
	}

	@Override
	public void onRecognizeStatusChanged(String status) {
		this.status.setText(status);
	}
}
