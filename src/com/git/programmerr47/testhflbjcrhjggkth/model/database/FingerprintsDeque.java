package com.git.programmerr47.testhflbjcrhjggkth.model.database;

import java.util.concurrent.LinkedBlockingDeque;

import android.util.Log;
import com.git.programmerr47.testhflbjcrhjggkth.model.FingerprintData;

public class FingerprintsDeque<FingerprintData> extends LinkedBlockingDeque<FingerprintData>{

	private OnDequeStateListener listener;
	/**
	 * 
	 */
	private static final long serialVersionUID = -3309742009327045074L;
	
	@Override
	public synchronized void addFirst(FingerprintData data) {
		super.addFirst(data);
        if (size() == 1) {
            Log.v("FingersQueue", "Now queue is not empty");
            if (listener != null) {
                listener.onNonEmpty();
            }
        }
	}
	
	@Override
	public synchronized void addLast(FingerprintData data) {
		super.addLast(data);
        Log.v("Fingers", "deque.size = " + size());
		if (size() == 1) {
            Log.v("FingersQueue", "Now queue is not empty");
            if (listener != null) {
                listener.onNonEmpty();
            }
        }
	}
	
	public void setOnDequeStateListener(OnDequeStateListener listener) {
		this.listener = listener;
	}
	
	public interface OnDequeStateListener {
		void onNonEmpty();
	}
	
	@Override
	public synchronized FingerprintData pollFirst() {
		FingerprintData result = super.pollFirst();
		if (size() > 0) {
            if (listener != null) {
                listener.onNonEmpty();
            }
		}
		return result;
	}
}