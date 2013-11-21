package com.git.programmerr47.testhflbjcrhjggkth.model.observers;

import com.git.programmerr47.testhflbjcrhjggkth.model.database.data.SongData;

public interface IRecognizeResultObservable {
	
	void addObserver(IRecognizeResultObserver o);
	void removeObserver(IRecognizeResultObserver o);
	void notifyRecognizeResultObservers(SongData songData);
}