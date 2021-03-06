package com.github.spitsinstafichuk.vkazam.services;

import com.github.spitsinstafichuk.vkazam.model.SongData;

/**
 * Service that provides recording audio from microphone
 * Relates with GraceNote and receives fingerprint
 * generated by end of listening
 *
 * @author Michael Spitsin
 * @since 2014-04-13
 */
public class MicrophoneRecordingNowService extends MicrophoneRecordingService {

    @Override
    public void recordFingerprint() {
        startServiceWorking();
        defRecordingMethod();
    }

    @Override
    public void cancelRecording() {
        defCancelingMethod();
        stopWorking();
    }

    @Override
    protected int getFingerprintPriority() {
        return FingerprintWrapper.RECOGNIZE_PRIORITY_HIGHEST;
    }

    @Override
    public void onResultStatus(SongData data) {
        super.onResultStatus(data);
        stopWorking();
    }
}
