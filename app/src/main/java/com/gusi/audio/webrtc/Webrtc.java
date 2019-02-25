package com.gusi.audio.webrtc;

/**
 * @Author ylw  2019/2/24 18:31
 */
public class Webrtc {
    static {
        System.loadLibrary("Webrtc");
    }

    /**
     * 音频降噪
     *
     * @param file_in  输入文件
     * @param file_out 输出文件
     * @param sample   采样率
     * @param mode     降噪强度0-2
     */
    public static native void noiseSuppression(String file_in, String file_out, int sample, int mode);

    /**
     * 音频降噪
     *
     * @param buffer 音频文件流
     * @param sample 同上
     * @param mode   同上
     */
    public static native void noiseSuppressionByBytes(byte[] buffer, int sample, int mode);
}
