package com.gusi.audio.speex;

/**
 * @Author ylw  2019/3/3 13:43
 */
public class Speex {
    static {
        System.loadLibrary("Speex");
    }

    public static native void CancelNoiseInit(int frame_size, int sample_rate);

    public static native void CancelNoisePreprocess(byte[] inbuffer);

    public static native void CancelNoiseDestroy();

    /**
     * jint frame_size        帧长      一般都是  80,160,320
     * jint filter_length     尾长      一般都是  80*25 ,160*25 ,320*25
     * jint sampling_rate     采样频率  一般都是  8000，16000，32000
     * 比如初始化
     * InitAudioAEC(80, 80*25,8000)   //8K，10毫秒采样一次
     * InitAudioAEC(160,160*25,16000) //16K，10毫秒采样一次
     * InitAudioAEC(320,320*25,32000) //32K，10毫秒采样一次
     */
    public static native void InitAudioAEC(int frameSize, int filterLen, int samplingRate);

    /**
     * 参数：
     * jbyteArray recordArray  录音数据
     * jbyteArray playArray    放音数据
     * jbyteArray szOutArray
     */
    public static native void AudioAECProc(byte[] recordBys, byte[] playBys, byte[] outBys);

    public static native void ExitSpeexDsp();
}
