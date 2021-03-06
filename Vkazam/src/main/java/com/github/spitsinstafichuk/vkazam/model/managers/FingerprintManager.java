package com.github.spitsinstafichuk.vkazam.model.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.spitsinstafichuk.vkazam.model.database.DBConstants;
import com.github.spitsinstafichuk.vkazam.model.observers.*;
import com.gracenote.mmid.MobileSDK.GNConfig;
import com.gracenote.mmid.MobileSDK.GNFingerprintResult;
import com.gracenote.mmid.MobileSDK.GNFingerprintResultReady;
import com.gracenote.mmid.MobileSDK.GNOperationStatusChanged;
import com.gracenote.mmid.MobileSDK.GNOperations;
import com.gracenote.mmid.MobileSDK.GNStatus;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class FingerprintManager
        implements
        IFingerprintStatusObservable,
        IFingerprintResultObservable,
        IFingerprintTimerObservable,
        GNOperationStatusChanged,
        GNFingerprintResultReady {

    public static final int DEFAULT_FINGERPRINT_TIMER_PERIOD = 5 * 1000;

    public static final String FINGERPRINTING_SUCCESS = "Fingerprinting success";

    public static final String TAG = "FingerprintManager";

    private GNConfig config;

    private Handler handler;

    private Set<IFingerprintStatusObserver> fingerprintStatusObservers;

    private Set<IFingerprintResultObserver> fingerprintResultObservers;

    private Set<IFingerprintTimerObserver> fingerprintTimerObservers;

    private int fingerprintTimerPeriod;

    private ScheduledThreadPoolExecutor fingerprintTimer;

    private volatile boolean isFingerprinting;

    private boolean isFingerprintingByTimer;

    private boolean isFingerprintingOneTime;

    public FingerprintManager(GNConfig config, Context context, Handler handler) {
        fingerprintStatusObservers = new HashSet<IFingerprintStatusObserver>();
        fingerprintResultObservers = new HashSet<IFingerprintResultObserver>();
        fingerprintTimerObservers = new HashSet<IFingerprintTimerObserver>();
        fingerprintTimerPeriod = DEFAULT_FINGERPRINT_TIMER_PERIOD;
        isFingerprinting = false;
        isFingerprintingByTimer = false;
        isFingerprintingOneTime = false;
        this.config = config;
        this.handler = handler;
    }

    public void fingerprintByTimer() {
        isFingerprintingByTimer = true;
        isFingerprintingOneTime = false;
        fingerprint();
    }

    public void fingerprintOneTime() {
        isFingerprintingByTimer = false;
        isFingerprintingOneTime = true;
        fingerprint();
    }

    private void fingerprint() {
        if (!isFingerprinting) {
            synchronized (this) {
                if (!isFingerprinting) {
                    isFingerprinting = true;
                    GNOperations.fingerprintMIDStreamFromMic(this, config);
                }
            }
        }
    }


    public void fingerprintCancel() {
        GNOperations.cancel(this);
        isFingerprinting = false;
        isFingerprintingByTimer = false;
        isFingerprintingOneTime = false;
    }

    public boolean isFingerprintingOneTime() {
        return isFingerprintingOneTime;
    }

    public boolean isFingerprintingByTimer() {
        return isFingerprintingByTimer;
    }

    @Override
    public void GNStatusChanged(GNStatus status) {
        String fingerprintStatus = status.getMessage() + " " + status.getPercentDone() + " %";
        notifyFingerprintStatusObserversOnUiThread(fingerprintStatus);
    }

    @Override
    public void GNResultReady(GNFingerprintResult result) {
        isFingerprinting = false;
        isFingerprintingOneTime = false;
        String fingerprintStatus = null;
        String fingerprint = null;
        int errCode = 0;
        if (result.isFailure()) {
            errCode = result.getErrCode();
            fingerprintStatus = String.format("[%d] %s", errCode, result.getErrMessage());
        } else {
            fingerprintStatus = FINGERPRINTING_SUCCESS;
            fingerprint = result.getFingerprintData();
            Log.i(TAG, "fingerprint = " + fingerprint);
        }
        notifyFingerprintStatusObserversOnUiThread(fingerprintStatus);
        notifyFingerprintResultObserversOnUiThread(errCode, fingerprint);
    }

    @Override
    public void addFingerprintStatusObserver(IFingerprintStatusObserver o) {
        fingerprintStatusObservers.add(o);
    }

    @Override
    public void removeFingerprintStatusObserver(IFingerprintStatusObserver o) {
        fingerprintStatusObservers.remove(o);
    }

    private void notifyFingerprintStatusObserversOnUiThread(final String status) {
        handler.post(new Runnable() {
            public void run() {
                notifyFingerprintStatusObservers(status);
            }
        });
    }

    private void notifyFingerprintResultObserversOnUiThread(final int errorCode,
            final String fingerprint) {
        handler.post(new Runnable() {
            public void run() {
                notifyFingerprintResultObservers(errorCode, fingerprint);
            }
        });
    }

    @Override
    public void addFingerprintResultObserver(IFingerprintResultObserver o) {
        fingerprintResultObservers.add(o);
    }

    @Override
    public void removeFingerprintResultObserver(IFingerprintResultObserver o) {
        fingerprintResultObservers.remove(o);
    }

    @Override
    public void notifyFingerprintResultObservers(int errorCode, String fingerprint) {
        for (IFingerprintResultObserver o : fingerprintResultObservers) {
            o.onFingerprintResult(errorCode, fingerprint);
        }
    }

    @Override
    public void notifyFingerprintStatusObservers(String status) {
        for (IFingerprintStatusObserver o : fingerprintStatusObservers) {
            o.onFingerprintStatusChanged(status);
        }
    }

    @Override
    public void addFingerprintTimerObserver(IFingerprintTimerObserver o) {
        fingerprintTimerObservers.add(o);
    }

    @Override
    public void removeFingerprintTimerObserver(IFingerprintTimerObserver o) {
        fingerprintTimerObservers.remove(o);
    }

    @Override
    public void notifyFingerprintObservers() {
        for (IFingerprintTimerObserver o : fingerprintTimerObservers) {
            o.onFingerprintTimerUpdated();
        }
    }
}
