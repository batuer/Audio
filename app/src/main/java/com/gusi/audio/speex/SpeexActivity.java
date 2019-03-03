package com.gusi.audio.speex;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.gusi.audio.AudioEffectEntity;
import com.gusi.audio.R;
import com.gusi.audio.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpeexActivity extends Activity {

    private CheckBox mCbSystem;
    private CheckBox mCbDsp;
    private CheckBox mCbAudioEffect;
    private TextView mTvPcm;
    private SpeexAudio mSpeexAudio;
    private List<File> mFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speex);
        mCbSystem = findViewById(R.id.cb_system);
        mCbDsp = findViewById(R.id.cb_dsp);
        mCbAudioEffect = findViewById(R.id.cb_audio_effect);
        mTvPcm = findViewById(R.id.tv_pcm);
        mSpeexAudio = new SpeexAudio();
        mFileList = new ArrayList<>();
    }

    public void record(View view) {
        int audioSource = mCbSystem.isChecked() ? MediaRecorder.AudioSource.REMOTE_SUBMIX : MediaRecorder.AudioSource.MIC;
        mSpeexAudio.recordByte(audioSource, new AudioEffectEntity(true, true), mCbDsp.isChecked());
    }


    public void stopRecord(View view) {
        mSpeexAudio.stopRecord();
        //
        final File recordFile = mSpeexAudio.getAudioRecordFile();
        if (recordFile != null) {
            mFileList.add(recordFile);
            StringBuilder sb = new StringBuilder();
            String s = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/recorderDemo/";
            for (File file1 : mFileList) {
                String path = file1.getPath();
                sb.append(path.replace(s, "") + "\n");
            }
            mTvPcm.setText(sb.toString());
            //
        }
    }

    public void play(View view) {
        File recordFile = mSpeexAudio.getAudioRecordFile();
        if (recordFile == null) {
            ToastUtils.showShort("recordFile = " + recordFile);
        } else {
            mSpeexAudio.playByte(recordFile);
        }
    }

    public void stopPlay(View view) {
        mSpeexAudio.stopPlay();
    }
}
