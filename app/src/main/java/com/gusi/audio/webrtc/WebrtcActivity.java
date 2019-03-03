package com.gusi.audio.webrtc;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.system.Os;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.gusi.audio.AudioEffectEntity;
import com.gusi.audio.R;
import com.gusi.audio.speex.SpeexActivity;
import com.gusi.audio.utils.PCM;
import com.gusi.audio.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WebrtcActivity extends Activity {

    private TextView mTvPcm;
    private EditText mEtWeight;
    private CheckBox mCbAgcNs, mCbPlayByte, mCbRecordByte, mCbSystem, mCbAudioEffect;
    private WebrtcAudio mWebrtcAudio;
    private List<File> mFileList;
    private File mMixFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webrtc);
        mTvPcm = (TextView) findViewById(R.id.tv_pcm);
        mEtWeight = (EditText) findViewById(R.id.et_weight);
        mCbAgcNs = (CheckBox) findViewById(R.id.cb_agc_ns);
        mCbPlayByte = (CheckBox) findViewById(R.id.cb_play_byte);
        mCbRecordByte = (CheckBox) findViewById(R.id.cb_record_byte);
        mCbSystem = (CheckBox) findViewById(R.id.cb_system);
        mCbAudioEffect = (CheckBox) findViewById(R.id.cb_effect);

        mWebrtcAudio = new WebrtcAudio();
        mFileList = new ArrayList<>();
        //
        if (PackageManager.PERMISSION_GRANTED != checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Process.myPid(), Os.getuid())) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
            requestPermissions(permissions, 100);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //添加菜单项(组ID,当前选项ID,排序，标题)
        menu.add(0, 100, 1, "Speex");
        return super.onCreateOptionsMenu(menu);
    }

    //菜单选项的单击事件处理方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case 100:
                startActivity(new Intent(this, SpeexActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
        boolean aec = mCbAudioEffect.isChecked();
        if (mCbRecordByte.isChecked()) {
            mWebrtcAudio.recordAndPlayByte(audioSource, new AudioEffectEntity(aec, aec));
        } else {
            mWebrtcAudio.recordAndPlayShort(audioSource, new AudioEffectEntity(aec, aec));
        }
    }

    public void record(View view) {
        int audioSource = mCbSystem.isChecked() ? MediaRecorder.AudioSource.REMOTE_SUBMIX : MediaRecorder.AudioSource.MIC;
        boolean aec = mCbAudioEffect.isChecked();
        if (mCbRecordByte.isChecked()) {
            mWebrtcAudio.recordByte(audioSource, new AudioEffectEntity(aec, aec));
        } else {
            mWebrtcAudio.recordShort(audioSource, new AudioEffectEntity(aec, aec));
        }
//        WebRtcAudioRecord webRtcAudioRecord = new WebRtcAudioRecord
    }

    public void stopRecord(View view) {
        mWebrtcAudio.stopRecord();
        //
        final File recordFile = mWebrtcAudio.getAudioRecordFile();
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
        File recordFile = mWebrtcAudio.getAudioRecordFile();
        if (recordFile == null) {
            ToastUtils.showShort("RecordFile: " + recordFile);
        } else {
            if (mCbPlayByte.isChecked()) {
                mWebrtcAudio.playByte(recordFile, mCbAgcNs.isChecked());
            } else {
                mWebrtcAudio.playShort(recordFile);
            }
        }
    }

    public void stopPlay(View view) {
        mWebrtcAudio.stopPlay();
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
            mWebrtcAudio.playByte(mMixFile, mCbAgcNs.isChecked());
        } else {
            mWebrtcAudio.playShort(mMixFile);
        }
    }
}
