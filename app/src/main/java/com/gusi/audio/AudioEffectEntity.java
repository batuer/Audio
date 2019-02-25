package com.gusi.audio;

import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;

/**
 * @Author ylw  2019/2/21 20:38
 */
public class AudioEffectEntity {
    public AcousticEchoCanceler mAcousticEchoCanceler;
    public NoiseSuppressor mNoiseSuppressor;
    public boolean mIsAcousticEchoCanceler = true;
    public boolean mIsNoiseSuppressor = true;

    public AudioEffectEntity(boolean b) {
        mIsAcousticEchoCanceler = b;
        mIsNoiseSuppressor = b;
    }


    public void noise(int audioSessionId) {
        boolean available = AcousticEchoCanceler.isAvailable();
        if (mIsAcousticEchoCanceler && available) {
            mAcousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId);
            if (mAcousticEchoCanceler != null) {
                mAcousticEchoCanceler.setEnabled(true);
            }
        }
        boolean available1 = NoiseSuppressor.isAvailable();
        if (mIsNoiseSuppressor && available1) {
            mNoiseSuppressor = NoiseSuppressor.create(audioSessionId);
            if (mNoiseSuppressor != null) {
                mNoiseSuppressor.setEnabled(true);
            }
        }
        Log.w("Fire", available + ":" + available1 + "AudioEffectEntity:34行:" + toString());
    }

    public void release() {
        if (mNoiseSuppressor != null) {
            mNoiseSuppressor.release();
        }
        if (mAcousticEchoCanceler != null) {
            mAcousticEchoCanceler.release();
        }
    }

    @Override
    public String toString() {
        return "AudioEffectEntity{" +
                "mAcousticEchoCanceler=" + mAcousticEchoCanceler +
                ", mNoiseSuppressor=" + mNoiseSuppressor +
                ", mIsAcousticEchoCanceler=" + mIsAcousticEchoCanceler +
                ", mIsNoiseSuppressor=" + mIsNoiseSuppressor +
                '}';
    }
}
