package com.git.programmerr47.testhflbjcrhjggkth.model.database;

import java.util.List;

import com.git.programmerr47.testhflbjcrhjggkth.model.database.data.ISongData;


public interface ISongDAO {
	List<ISongData> getHistory();
	long insert(ISongData songData);
	int delete(ISongData songData);
	int update(ISongData songData);
}
