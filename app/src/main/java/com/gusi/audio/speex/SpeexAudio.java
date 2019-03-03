package com.gusi.audio.speex;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import com.gusi.audio.AudioEffectEntity;
import com.gusi.audio.utils.CloseUtils;
import com.gusi.audio.utils.ToastUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author ylw  2019/3/3 13:43
 */
public class SpeexAudio {
    private volatile boolean mIsRecording;
    private volatile boolean mIsPlaying;
    private final ExecutorService mService;
    private volatile File mAudioRecordFile;


    //录音时采用的采样频率，所以播放时同样的采样频率
    private int sampleRate = 44100;
    //配置录音器
    //单声道输入
    int inChannelConfig = AudioFormat.CHANNEL_IN_MONO;
    //PCM_16是所有android系统都支持的
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    //配置播放器
    //音乐类型，扬声器播放
    int streamType = AudioManager.STREAM_MUSIC;
    //流模式
    int mode = AudioTrack.MODE_STREAM;
    int outChannelConfig = AudioFormat.CHANNEL_OUT_MONO;

    public SpeexAudio() {
        mService = Executors.newSingleThreadExecutor();
    }

    public void recordByte(final int audioSource, final AudioEffectEntity effectEntity, boolean speexDsp) {
        ToastUtils.showShort("recordByte 开始");
        if (mIsRecording) {
            ToastUtils.showShort("Audio正在录音: ");
            return;
        }
        mService.execute(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    //创建录音文件
                    mAudioRecordFile = new File(Environment.getExternalStorageDirectory()
                            .getAbsolutePath() +
                            "/recorderDemo/" + System.currentTimeMillis() + ".pcm");
                    File recordFileParentFile = mAudioRecordFile.getParentFile();
                    if (!recordFileParentFile.exists()) recordFileParentFile.mkdirs();
                    mAudioRecordFile.createNewFile();
                    //创建文件输出流
                    fos = new FileOutputStream(mAudioRecordFile);

                    //计算AudioRecord内部buffer最小
                    int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, inChannelConfig, audioFormat);
                    //buffer不能小于最低要求，也不能小于我们每次我们读取的大小。
                    AudioRecord audioRecord = new AudioRecord(audioSource, sampleRate, inChannelConfig, audioFormat, minBufferSize);
                    //开始录音
                    mIsRecording = true;
                    audioRecord.startRecording();
                    effectEntity.noise(audioRecord.getAudioSessionId());

                    byte[] buffer = new byte[minBufferSize];
                    //循环读取数据，写入输出流中
                    while (mIsRecording) {
                        //只要还在录音就一直读取
                        int read = audioRecord.read(buffer, 0, minBufferSize);
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            fos.write(buffer, 0, minBufferSize);
                        }
                    }
                    //退出循环，停止录音，释放资源
                    if (audioRecord != null) {
                        audioRecord.release();
                    }

                } catch (IOException e) {
                    ToastUtils.showShort(e.toString());
                    Log.e("Fire", "MediaActivity:163行:" + e.toString());
                } finally {
                    mIsRecording = false;
                    effectEntity.release();
                    CloseUtils.closeIO(fos);
                    ToastUtils.showShort("recordByte 结束");
                }
            }
        });
    }

    public void stopRecord() {
        ToastUtils.showShort("stopRecord  ");
        if (!mIsRecording) {
            ToastUtils.showShort("Audio 没有在录音!");
            return;
        }
        mIsRecording = false;
    }

    public void playByte(final File file) {
        ToastUtils.showShort("playByte  开始");
        if (mIsRecording) {
            ToastUtils.showShort("Audio 正在录音!");
            return;
        }
        if (mIsPlaying) {
            ToastUtils.showShort("Audio 正在播放!");
            return;
        }
        mService.execute(new Runnable() {
            @Override
            public void run() {
                String file_out = Environment.getExternalStorageDirectory() + "/123.pcm";
                //配置播放器
                //计算最小buffer大小
                int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, outChannelConfig, audioFormat);
                //构造AudioTrack  不能小于AudioTrack的最低要求，也不能小于我们每次读的大小
                AudioTrack audioTrack = new AudioTrack(streamType, sampleRate, outChannelConfig, audioFormat, minBufferSize, mode);
                //开始播放
                mIsPlaying = true;
                audioTrack.play();
                byte[] buffer = new byte[minBufferSize];
                //从文件流读数据
                FileInputStream is = null;
                try {
                    //循环读数据，写到播放器去播放
                    is = new FileInputStream(file);
                    //循环读数据，写到播放器去播放

                    //只要没读完，循环播放
                    while (mIsPlaying && (is.read(buffer) > 0)) {
                        int ret = audioTrack.write(buffer, 0, minBufferSize);
                        //检查write的返回值，处理错误
                        switch (ret) {
                            case AudioTrack.ERROR_INVALID_OPERATION:
                            case AudioTrack.ERROR_BAD_VALUE:
                            case AudioManager.ERROR_DEAD_OBJECT:
                                ToastUtils.showShort("播放失败!");
                                return;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    Log.e("Fire", "MediaActivity:208行:" + e.toString());
                    //读取失败
                    ToastUtils.showShort("播放失败!" + e.toString());
                } finally {
                    mIsPlaying = false;
                    //播放器释放
                    audioTrack.release();
                    //关闭文件输入流
                    CloseUtils.closeIO(is);
                    ToastUtils.showShort("playByte  结束");
                }
                //循环读数据，写到播放器去播放
                //错误处理，防止闪退
            }
        });
    }

    public void stopPlay() {
        ToastUtils.showShort("stopPlay ");
        if (!mIsPlaying) {
            ToastUtils.showShort("没有在播放!");
        } else {
            mIsPlaying = false;
        }
    }

    public File getAudioRecordFile() {
        return mAudioRecordFile;
    }
}
