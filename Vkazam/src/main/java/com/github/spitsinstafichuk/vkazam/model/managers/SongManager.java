package com.github.spitsinstafichuk.vkazam.model.managers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.spitsinstafichuk.vkazam.model.MicroScrobblerMediaPlayer;
import com.github.spitsinstafichuk.vkazam.model.database.DatabaseSongData;
import com.github.spitsinstafichuk.vkazam.model.exceptions.SongNotFoundException;
import com.github.spitsinstafichuk.vkazam.model.exceptions.VkAccountNotFoundException;
import com.github.spitsinstafichuk.vkazam.model.observers.ISongInfoObserver;
import com.github.spitsinstafichuk.vkazam.model.observers.ISongInfoObserverable;
import com.github.spitsinstafichuk.vkazam.model.observers.ISongProgressObservable;
import com.github.spitsinstafichuk.vkazam.model.observers.ISongProgressObserver;
import com.github.spitsinstafichuk.vkazam.model.pleer.api.KException;
import com.github.spitsinstafichuk.vkazam.utils.NetworkUtils;

public class SongManager implements ISongInfoObserverable, ISongProgressObservable {

    private final static String TAG = "SongManager";

    public static final int NO_SONG = -1;

    public static final int ANY_SONG = 0;

    public static final int VK_SONG = 1;

    public static final int PP_SONG = 2;

    private Set<ISongInfoObserver> songInfoObservers;

    private Set<ISongProgressObserver> songProgressObservers;

    private MicroScrobblerMediaPlayer songPlayer;

    private MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener;

    private MediaPlayer.OnCompletionListener onCompletionListener;

    private DatabaseSongData songData;

    private int positionInList;

    private int type = ANY_SONG;

    private Handler handler;

    private Context context;

    private Scrobbler scrobbler;

    private ScheduledThreadPoolExecutor songProgressTimer;

    private boolean wasPlayed = false;

    private com.perm.kate.api.Api vkApi;

    public SongManager(Handler handler, Context context, Scrobbler scrobbler) {
        songPlayer = MicroScrobblerMediaPlayer.getInstance();
        this.handler = handler;
        this.context = context;
        this.scrobbler = scrobbler;
        songInfoObservers = new HashSet<ISongInfoObserver>();
        songProgressObservers = new HashSet<ISongProgressObserver>();

        songProgressTimer = new ScheduledThreadPoolExecutor(1);
        songProgressTimer.scheduleWithFixedDelay(new Thread() {

            @Override
            public void run() {
                if ((songData != null)) {
                    asyncNotifySongProgressObservers();
                }
            }

        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void set(DatabaseSongData songData, int positionInList, com.perm.kate.api.Api vkApi) {
        this.vkApi = vkApi;
        this.songData = songData;
        this.positionInList = positionInList;
        wasPlayed = false;
        //isPrepared = false;
        asyncNotifySongInfoObservers();
    }

    private boolean findPPAudio()
            throws MalformedURLException, IOException, JSONException, KException,
            SongNotFoundException {
        if (songData.getPleercomUrl() == null) {
            songData.findPPAudio();
            Log.i(TAG, "new Pleercomurl: " + songData.getPleercomUrl());
            songPlayer.setDataSource(songData.getPleercomUrl());
        } else {
            boolean networkWasAvailable = false;
            try {
                Log.i(TAG, "Pleercomurl: " + songData.getPleercomUrl());
                networkWasAvailable = NetworkUtils.isNetworkAvailable(context);
                songPlayer.setDataSource(songData.getPleercomUrl());
            } catch (IOException e) {
                if (networkWasAvailable) {
                    songData.setPleercomUrl(null);
                    findPPAudio();
                }
            } catch (IllegalArgumentException e) {
                songData.findPPAudio();
                if (networkWasAvailable) {
                    songData.setPleercomUrl(null);
                    findPPAudio();
                }
            }
        }
        return true;
    }

    private boolean findVkAudio()
            throws MalformedURLException, IOException, JSONException, com.perm.kate.api.KException,
            SongNotFoundException, KException {
        if (songData.getVkAudioId() == null) {
            String vkUrl;
            vkUrl = songData.findVkAudio(vkApi);
            Log.i(TAG, "vk audio id: " + songData.getVkAudioId());
            Log.i(TAG, "vk audio url: " + vkUrl);
            songPlayer.setDataSource(vkUrl);
        } else {
            List<com.perm.kate.api.Audio> audioList;
            audioList = vkApi.getAudioById(songData.getVkAudioId(), null, null);
            if (audioList.isEmpty()) {
                songData.setVkAudioId(null);
                findVkAudio();
            }
            String vkUrl = audioList.get(0).url;
            Log.i(TAG, "vk audio id: " + songData.getVkAudioId());
            Log.i(TAG, "vk audio url: " + vkUrl);
            songPlayer.setDataSource(vkUrl);
        }
        return true;
    }

    //есть сомнения по поводу корректности проверки рабочий ли url для песни перед попыткой его обновить: возможно, помимо setDataSource, стоит также вызывать prepare
    public void prepare(int type)
            throws IOException, JSONException, SongNotFoundException, KException,
            com.perm.kate.api.KException, VkAccountNotFoundException {
        Log.v(TAG, "Player is loading");
        this.type = type;
        songPlayer = MicroScrobblerMediaPlayer.getInstance();
        songPlayer.setLoadingState();
        songPlayer.setOnCompletionListener(onCompletionListener);
        songPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        Log.v(TAG, "Player is reconstructed");
        boolean found = false;
        if (type == PP_SONG) {
            found = findPPAudio();
        } else if (type == VK_SONG) {
            if (PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("settingsVkConnection", false)) {
                found = findVkAudio();
            } else {
                throw new VkAccountNotFoundException();
            }
        } else {
            if (!PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("settingsVkConnection", false) ||
                    !PreferenceManager.getDefaultSharedPreferences(context)
                            .getBoolean("settingsVkUrls", false)) {
                found = findPPAudio();
                this.type = PP_SONG;
                if (!found) {
                    if (PreferenceManager.getDefaultSharedPreferences(context)
                            .getBoolean("settingsVkConnection", false)) {
                        found = findVkAudio();
                        this.type = VK_SONG;
                    }
                }
            } else {
                found = findVkAudio();
                this.type = VK_SONG;
                if (!found) {
                    found = findPPAudio();
                    this.type = PP_SONG;
                }
            }
        }

        Log.v(TAG, "found: " + found);
        if (!found) {
            this.type = NO_SONG;
        }

        asyncNotifySongInfoObservers();

        if (!found) {
            throw new SongNotFoundException();
        }
        songPlayer.prepare();
    }

    public int getType() {
        return type;
    }

    public void play() {
        Log.v("SongListController",
                "Song" + songData.getArtist() + "-" + songData.getTitle() + "was started");
        if (!wasPlayed) {
            scrobbler.sendLastFMTrackStarted(getArtist(), getTitle(), songData.getAlbum(),
                    songPlayer.getDuration());
            if (PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("settingsVkAudioBroadcast", false)) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            vkApi.setStatus(null, songData.getVkAudioId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
            wasPlayed = true;
        } else {
            scrobbler.sendLastFMTrackUnpaused(getArtist(), getTitle(), songData.getAlbum(),
                    songPlayer.getDuration(), songPlayer.getCurrentPosition());
        }
        songPlayer.start();
    }

    public void pause() {
        scrobbler.sendLastFMTrackPaused(getArtist(), getTitle(), songData.getAlbum(),
                songPlayer.getDuration());
        songPlayer.pause();
    }

    public void stop() {
        songPlayer.stop();
    }

    public String getArtist() {
        return songData.getArtist();
    }

    public String getTitle() {
        return songData.getTitle();
    }

    public boolean isPlaying() {
        return songPlayer.isPlaying();
    }

    public DatabaseSongData getSongData() {
        return songData;
    }

    public int getPositionInList() {
        return positionInList;
    }

    public void release() {
        if (songData != null) {
            scrobbler.sendLastFMPlaybackCompleted(getArtist(), getTitle(), songData.getAlbum(),
                    songPlayer.getDuration());
        }
        this.type = NO_SONG;
        songPlayer.release();
        Log.v(TAG, "Player is released");
    }

    public boolean isLoading() {
        return songPlayer.isLoading();
    }

    public boolean isPrepared() {
        return songPlayer.isPrepared();
    }

    public synchronized void seekTo(int percent) {
        if (songPlayer.isPrepared()) {
            if (songPlayer.getDuration() != -1) {
                songPlayer.seekTo(songPlayer.getDuration() * percent / 100);
            }
        }
    }

    public synchronized void setOnBufferingUpdateListener(
            MediaPlayer.OnBufferingUpdateListener listener) {
        onBufferingUpdateListener = listener;
        if (songPlayer != null) {
            songPlayer.setOnBufferingUpdateListener(listener);
        }
    }

    public synchronized void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        onCompletionListener = listener;
        if (songPlayer != null) {
            songPlayer.setOnCompletionListener(listener);
        }
    }

    @Override
    public void addSongIngoObserver(ISongInfoObserver o) {
        songInfoObservers.add(o);
    }

    @Override
    public void removeSongIngoObserver(ISongInfoObserver o) {
        songInfoObservers.remove(o);
    }

    @Override
    public void notifySongInfoObservers() {
        for (ISongInfoObserver o : songInfoObservers) {
            o.updateSongInfo();
        }
    }

    private void asyncNotifySongInfoObservers() {
        handler.post(new Runnable() {
            public void run() {
                notifySongInfoObservers();
            }
        });
    }

    @Override
    public void addSongProgressObserver(ISongProgressObserver o) {
        songProgressObservers.add(o);
    }

    @Override
    public void removeSongProgressObserver(ISongProgressObserver o) {
        songProgressObservers.remove(o);
    }

    @Override
    public void notifySongProgressObservers(boolean isPrepared) {
        for (ISongProgressObserver o : songProgressObservers) {
            if (isPrepared) {
                o.updateProgress(songPlayer.getCurrentPosition(), songPlayer.getDuration());
            } else {
                o.updateProgress(0, -1);
            }
        }
    }

    private void asyncNotifySongProgressObservers() {
        handler.post(new Runnable() {
            public void run() {
                notifySongProgressObservers(isPrepared());
            }
        });
    }
}
