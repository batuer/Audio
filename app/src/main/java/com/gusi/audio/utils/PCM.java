package com.gusi.audio.utils;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * @Author ylw  2019/2/18 21:24
 */
public class PCM {
    /**
     * PCM文件转WAV文件
     *
     * @param inPcmFilePath  输入PCM文件路径
     * @param outWavFilePath 输出WAV文件路径
     * @param sampleRate     采样率，例如44100
     * @param channels       声道数 单声道：1或双声道：2
     * @param bitNum         采样位数，8或16
     */
    public static void convertPcm2Wav(String inPcmFilePath, String outWavFilePath, int sampleRate, int channels, int bitNum) {
        FileInputStream in = null;
        FileOutputStream out = null;
        byte[] data = new byte[1024];

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            in = new FileInputStream(inPcmFilePath);
            out = new FileOutputStream(outWavFilePath);

            //PCM文件大小
            long totalAudioLen = in.getChannel()
                    .size();

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);

            int length = 0;
            while ((length = in.read(data)) > 0) {
                out.write(data, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Fire", "PCM:56行:" + e.toString());
        } finally {
            CloseUtils.closeIO(in, out);
        }
    }


    /**
     * 输出WAV文件
     *
     * @param out           WAV输出文件流
     * @param totalAudioLen 整个音频PCM数据大小
     * @param totalDataLen  整个数据大小
     * @param sampleRate    采样率
     * @param channels      声道数
     * @param byteRate      采样字节byte率
     * @throws IOException
     */
    private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, int sampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    /**
     * 权值叠加
     *
     * @param bMulRoadAudioes 声源
     * @param weights         权值（直接叠加为1、平均叠加为0.5）weights.length = bMulRoadAudioes.length
     * @return
     */
    public static byte[] mixRawAudioBytes(byte[][] bMulRoadAudioes, double[] weights) {
        if (bMulRoadAudioes == null || bMulRoadAudioes.length == 0) return null;

        byte[] realMixAudio = bMulRoadAudioes[0];
        if (bMulRoadAudioes.length == 1)
            return realMixAudio;
        // FIXME: 2019/2/18 数组等长
        for (int i = 0; i < bMulRoadAudioes.length; ++i) {
            if (bMulRoadAudioes[i].length != realMixAudio.length) {
                Log.e("Fire", "PCM:141行:column of the road of audio + " + i + " is diffrent.");
                return null;
            }
        }

        //row 代表参与合成的音频数量
        //column 代表一段音频的采样点数，这里所有参与合成的音频的采样点数都是相同的
        int row = bMulRoadAudioes.length;
        int column = realMixAudio.length / 2;
        short[][] sMulRoadAudioes = new short[row][column];

        //PCM音频16位的存储是大端存储方式，即低位在前，高位在后，例如(X1Y1, X2Y2, X3Y3)数据，
        // 它代表的采样点数值就是(（Y1 * 256 + X1）, （Y2 * 256 + X2）, （Y3 * 256 + X3）)
        for (int r = 0; r < row; ++r) {
            for (int c = 0; c < column; ++c) {
                sMulRoadAudioes[r][c] = (short) ((bMulRoadAudioes[r][c * 2] & 0xff) | (bMulRoadAudioes[r][c * 2 + 1] & 0xff) << 8);
            }
        }

        short[] sMixAudio = new short[column];
        int mixVal;
        int sr = 0;
        for (int sc = 0; sc < column; ++sc) {
            mixVal = 0;
            sr = 0;
            //这里采取累加法
            for (; sr < row; ++sr) {
//                mixVal += sMulRoadAudioes[sr][sc];
                double weight = weights[sr];//权值
                Log.w("Fire", "PCM:173行:" + weight);
                mixVal += sMulRoadAudioes[sr][sc] * weight;
            }
            //最终值不能大于short最大值，因此可能出现溢出
            sMixAudio[sc] = (short) (mixVal);
        }

        //short值转为大端存储的双字节序列
        for (sr = 0; sr < column; ++sr) {
            realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
            realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
        }

        return realMixAudio;
    }

    /**
     * 合成PCM 文件
     *
     * @param rawAudioFiles
     * @param destFile
     */
    public static void mixAudios(File[] rawAudioFiles, File destFile, double weight) {
        ToastUtils.showShort("开始合并PCM文件");
        final int fileSize = rawAudioFiles.length;
        FileInputStream[] audioFileStreams = new FileInputStream[fileSize];
        File audioFile = null;

        FileInputStream inputStream = null;
        OutputStream output = null;
        BufferedOutputStream bufferedOutput = null;
        byte[][] allAudioBytes = new byte[fileSize][];
        boolean[] streamDoneArray = new boolean[fileSize];
        byte[] buffer = new byte[512];
        int offset;

        try {
            output = new FileOutputStream(destFile);
            bufferedOutput = new BufferedOutputStream(output);


            for (int fileIndex = 0; fileIndex < fileSize; ++fileIndex) {
                audioFile = rawAudioFiles[fileIndex];
                audioFileStreams[fileIndex] = new FileInputStream(audioFile);
            }
            while (true) {
                for (int streamIndex = 0; streamIndex < fileSize; ++streamIndex) {
                    inputStream = audioFileStreams[streamIndex];
                    if (!streamDoneArray[streamIndex] && (offset = inputStream.read(buffer)) != -1) {
                        allAudioBytes[streamIndex] = Arrays.copyOf(buffer, buffer.length);
                    } else {
                        streamDoneArray[streamIndex] = true;
                        allAudioBytes[streamIndex] = new byte[512];
                    }
                }
                double[] weights = new double[allAudioBytes.length];
                for (int i = 0; i < allAudioBytes.length; i++) {
                    weights[i] = weight;
                }


                byte[] mixBytes = mixRawAudioBytes(allAudioBytes, weights);
                bufferedOutput.write(mixBytes);
                //mixBytes 就是混合后的数据

                boolean done = true;
                for (boolean streamEnd : streamDoneArray) {
                    if (!streamEnd) {
                        done = false;
                    }
                }

                if (done) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Fire", "PCM:235行:" + e.toString());
        } finally {
            CloseUtils.closeIOQuietly(audioFileStreams);
            CloseUtils.closeIOQuietly(output, bufferedOutput);
            ToastUtils.showShort("结束合并PCM文件");
        }
    }
}
