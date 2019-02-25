package com.gusi.audio;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.system.Os;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.gusi.audio.utils.PCM;
import com.gusi.audio.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private TextView mTvPcm, mTvStatus;
    private EditText mEtWeight;
    private CheckBox mCbAgcNs, mCbPlayByte, mCbRecordByte, mCbSystem, mCbAudioEffect;
    private Audio2 mAudio2;
    private List<File> mFileList;
    private File mMixFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvPcm = (TextView) findViewById(R.id.tv_pcm);
        mTvStatus = (TextView) findViewById(R.id.tv_status);
        mEtWeight = (EditText) findViewById(R.id.et_weight);
        mCbAgcNs = (CheckBox) findViewById(R.id.cb_agc_ns);
        mCbPlayByte = (CheckBox) findViewById(R.id.cb_play_byte);
        mCbRecordByte = (CheckBox) findViewById(R.id.cb_record_byte);
        mCbSystem = (CheckBox) findViewById(R.id.cb_system);
        mCbAudioEffect = (CheckBox) findViewById(R.id.cb_effect);

        mAudio2 = new Audio2(this);
        mFileList = new ArrayList<>();
        //
        if (PackageManager.PERMISSION_GRANTED != checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Process.myPid(), Os.getuid())) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
            requestPermissions(permissions, 100);
        }
    }

    public void changeStatus(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvStatus.setText(msg);
            }
        });
    }

    public void clear(View view) {
        mFileList.clear();
        mTvPcm.setText("");
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/recorderDemo/");
        if (file.exists()) {
            for (File listFile : file.listFiles()) {
                listFile.delete();
            }
        }
    }

    public void startPlay(View view) {
        int audioSource = mCbSystem.isChecked() ? MediaRecorder.AudioSource.REMOTE_SUBMIX : MediaRecorder.AudioSource.MIC;
        if (mCbRecordByte.isChecked()) {
            mAudio2.recordAndPlayByte(audioSource, new AudioEffectEntity(mCbAudioEffect.isChecked()));
        } else {
            mAudio2.recordAndPlayShort(audioSource, new AudioEffectEntity(mCbAudioEffect.isChecked()));
        }
    }

    public void record(View view) {
        int audioSource = mCbSystem.isChecked() ? MediaRecorder.AudioSource.REMOTE_SUBMIX : MediaRecorder.AudioSource.MIC;
        if (mCbRecordByte.isChecked()) {
            mAudio2.recordByte(audioSource, new AudioEffectEntity(mCbAudioEffect.isChecked()));
        } else {
            mAudio2.recordShort(audioSource, new AudioEffectEntity(mCbAudioEffect.isChecked()));
        }
//        WebRtcAudioRecord webRtcAudioRecord = new WebRtcAudioRecord
    }

    public void stopRecord(View view) {
        mAudio2.stopRecord();
        //
        final File recordFile = mAudio2.getAudioRecordFile();
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
        File recordFile = mAudio2.getAudioRecordFile();
        if (recordFile == null) {
            ToastUtils.showShort("RecordFile: " + recordFile);
        } else {
            if (mCbPlayByte.isChecked()) {
                mAudio2.playByte(recordFile, mCbAgcNs.isChecked());
            } else {
                mAudio2.playShort(recordFile);
            }
        }
    }

    public void stopPlay(View view) {
        mAudio2.stopPlay();
    }

    public void mix(View view) {
        if (mFileList.size() <= 1) {
            ToastUtils.showShort("FileList.size() = " + mFileList.size());
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                double weight = Double.parseDouble(mEtWeight.getText()
                        .toString()
                        .trim());
                if (weight < 0) {
                    weight = 0.1;
                }
                if (weight > 1) {
                    weight = 1;
                }

                mMixFile = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() +
                        "/recorderDemo/mix" + System.currentTimeMillis() + ".pcm");
                PCM.mixAudios(mFileList.toArray(new File[mFileList.size()]), mMixFile, weight);
            }
        }).start();
    }

    public void mixPlay(View view) {
        if (mMixFile == null) {
            ToastUtils.showShort("没有合并的PCM文件:" + mMixFile);
            return;
        }
        if (mCbPlayByte.isChecked()) {
            mAudio2.playByte(mMixFile, mCbAgcNs.isChecked());
        } else {
            mAudio2.playShort(mMixFile);
        }
    }
}
