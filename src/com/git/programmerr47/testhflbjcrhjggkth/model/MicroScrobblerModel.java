package com.git.programmerr47.testhflbjcrhjggkth.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.git.programmerr47.testhflbjcrhjggkth.model.database.data.ISongData;
import com.git.programmerr47.testhflbjcrhjggkth.model.lastfm.IScrobbler;
import com.git.programmerr47.testhflbjcrhjggkth.model.lastfm.Scrobbler;
import com.git.programmerr47.testhflbjcrhjggkth.model.lastfm.Scrobbler.IOnSignInResultListener;
import com.git.programmerr47.testhflbjcrhjggkth.model.managers.ISongManager;
import com.git.programmerr47.testhflbjcrhjggkth.model.managers.RecognizeManager;
import com.git.programmerr47.testhflbjcrhjggkth.model.managers.SongManager;
import com.git.programmerr47.testhflbjcrhjggkth.model.observers.ISignInObservable;

public class MicroScrobblerModel implements IMicroScrobblerModel, ISignInObservable, IOnSignInResultListener {
	private static final String SAVE_LASTFM_INFO_PREF = "save lastfm info pref";
	private static final String LASTFM_USERNAME = "lastfm username";
	private static final String LASTFM_PASSWORD = "lastfm password";
	private static final int MODE = Activity.MODE_PRIVATE;
	public static final String RECOGNIZING_SUCCESS = "Success";
	
	private static MicroScrobblerModel instance;
	private static Context context;
	
	private Scrobbler scrobbler;
	private SharedPreferences sharedPreferences;
	
	private Set<IOnSignInResultListener> listeners;
	
	private ISongManager songManager;
	
	RecognizeManager recognizeManager;
	
	public static void setContext(Context con) {
		context = con;
	}
	
	public static synchronized MicroScrobblerModel getInstance() {
		if (instance == null) {
			instance = new MicroScrobblerModel();
		}
		
		return instance;
	}
	
	private MicroScrobblerModel() {
		songManager = new SongManager();
		listeners = new HashSet<IOnSignInResultListener>();
		scrobbler = new Scrobbler();
		sharedPreferences = context.getSharedPreferences(SAVE_LASTFM_INFO_PREF, MODE);
        final String login = sharedPreferences.getString(LASTFM_USERNAME, null);
        final String password = sharedPreferences.getString(LASTFM_PASSWORD, null);
        recognizeManager = new RecognizeManager(context, scrobbler);

        setLastfmAccount(login, password);
	}
	
	public RecognizeManager getRecognizeManager() {
		return recognizeManager;
	}
	
	@Override
	public List<ISongData> getHistory() {
		return recognizeManager.getHistory();
	}
	
	@Override
	public void setLastfmAccount(String username, String password) {
        scrobbler.signIn(username, password);
        scrobbler.setOnSignInResultListener(this);
	}
	
	@Override
	public void deleteLastfmAccount() {
		scrobbler.signOut();
	}

	@Override
	public IScrobbler getScrobbler() {
		return scrobbler;
	}

	@Override
    public void setOnSignInResultListener(IOnSignInResultListener listener) {
    	listeners.add(listener);
    }
    
    @Override
    public void removeOnSignInResultListener(IOnSignInResultListener listener) {
    	listeners.remove(listener);
    }
    
    @Override
    public void notifyOnSignInResultListener(String status) {
    	for(IOnSignInResultListener listener : listeners) {
    		listener.onResult(status);
    	}
    }

	@Override
	public void onResult(String status) {
		if(status.equals(STATUS_SUCCESS)) {
			SharedPreferences.Editor editor = sharedPreferences.edit();
	        editor.putString(LASTFM_USERNAME, scrobbler.getCorrectUsername());
	        editor.putString(LASTFM_PASSWORD, scrobbler.getCorrectPassword());
	        editor.commit();
	        songManager.setScrobbler(scrobbler);
		}
		notifyOnSignInResultListener(status);
	}
	
	public ISongManager getSongManager() {
		return songManager;
	}
}
