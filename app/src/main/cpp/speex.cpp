#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include <stdio.h>
#include <jni.h>
#include <string.h>
#include <stdio.h>
#include "speex/speex_preprocess.h"
#include "speex/speex_bits.h"
#include "speex/speex_echo.h"
#include "speex/speex_preprocess.h"
#include "speex.h"

#include <android/log.h>

static int nInitSuccessFlag = 0;
static int m_nFrameSize = 0;
static int m_nFilterLen = 0;
static int m_nSampleRate = 0;

static SpeexEchoState *m_pState;
static SpeexPreprocessState *m_pPreprocessorState;
static int iArg = 0;


#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "speex", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "speex", __VA_ARGS__))


SpeexPreprocessState *st;

JNIEXPORT jint JNICALL
Java_com_gusi_audio_speex_Speex_CancelNoiseInit(JNIEnv *env, jobject obj, jint frame_size,
                                                jint sample_rate) {

    int i;
    int count = 0;
    float f;

    st = speex_preprocess_state_init(frame_size / 2, sample_rate);

/*   i=1;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DENOISE, &i);
   i=0;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_AGC, &i);
   i=8000;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_AGC_LEVEL, &i);
   i=0;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB, &i);
   f=.0;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB_DECAY, &f);
   f=.0;
   speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB_LEVEL, &f); */

    i = 1;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DENOISE, &i);           /* 降噪 */
    i = -25;                                                                /* 负的32位整数 */
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_NOISE_SUPPRESS, &i);    /* 设置噪声的dB */
    i = 1;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_AGC, &i);               /* 增益 */
    i = 24000;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_AGC_LEVEL, &i);
    i = 0;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB, &i);
    f = .0;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB_DECAY, &f);
    f = .0;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB_LEVEL, &f);

    return 1;
}

JNIEXPORT jint JNICALL
Java_com_gusi_audio_speex_Speex_CancelNoisePreprocess(JNIEnv *env, jobject obj,
                                                      jbyteArray buffer
) {
    char *inbuffer = reinterpret_cast<char *>((env)->GetByteArrayElements(buffer, 0));

    short *in = reinterpret_cast<short *>(inbuffer);

    int vad = speex_preprocess_run(st, in);

    (env)->ReleaseByteArrayElements(buffer, (jbyte *) inbuffer, 0);

    return
            vad;
}

JNIEXPORT jint JNICALL Java_com_gusi_audio_speex_Speex_CancelNoiseDestroy(JNIEnv *env, jobject obj) {
    if (st != NULL) {
        speex_preprocess_state_destroy(st);
    }
    st = NULL;
    return 1;
}

/* 回声消除部分代码 */

/* 初始化回音消除参数 */


/*
 * jint frame_size        帧长      一般都是  80,160,320
 * jint filter_length     尾长      一般都是  80*25 ,160*25 ,320*25
 * jint sampling_rate     采样频率  一般都是  8000，16000，32000
 * 比如初始化
 *  InitAudioAEC(80, 80*25,8000)   //8K，10毫秒采样一次
 *  InitAudioAEC(160,160*25,16000) //16K，10毫秒采样一次
 *  InitAudioAEC(320,320*25,32000) //32K，10毫秒采样一次
 */
JNIEXPORT jint JNICALL Java_com_gusi_audio_speex_Speex_InitAudioAEC(JNIEnv *env, jobject thiz, jint frame_size,
                                                  jint filter_length, jint sampling_rate) {
    if (nInitSuccessFlag == 1)
        return (1);

    if (frame_size <= 0 || filter_length <= 0 || sampling_rate <= 0) {
        m_nFrameSize = 160;
        m_nFilterLen = 160 * 8;
        m_nSampleRate = 8000;
    } else {
        m_nFrameSize = frame_size;
        m_nFilterLen = filter_length;
        m_nSampleRate = sampling_rate;
    }

    m_pState = speex_echo_state_init(m_nFrameSize, m_nFilterLen);
    if (m_pState == NULL)
        return (-1);

    m_pPreprocessorState = speex_preprocess_state_init(m_nFrameSize, m_nSampleRate);
    if (m_pPreprocessorState == NULL)
        return (-2);

    iArg = m_nSampleRate;
    speex_echo_ctl(m_pState, SPEEX_ECHO_SET_SAMPLING_RATE, &iArg);
    speex_preprocess_ctl(m_pPreprocessorState, SPEEX_PREPROCESS_SET_ECHO_STATE, m_pState);
    nInitSuccessFlag = 1;
    return (1);
}


/*
 * 参数：
 * jbyteArray recordArray  录音数据
 * jbyteArray playArray    放音数据
 * jbyteArray szOutArray
 */
JNIEXPORT jint JNICALL Java_com_gusi_audio_speex_Speex_AudioAECProc(JNIEnv *env, jobject thiz,
                                                  jbyteArray recordArray,
                                                  jbyteArray playArray,
                                                  jbyteArray szOutArray) {
    if (nInitSuccessFlag == 0)
        return (0);

    jbyte *recordBuffer = (jbyte *) (env)->GetByteArrayElements(recordArray, 0);
    jbyte *playBuffer = (jbyte *) (env)->GetByteArrayElements(playArray, 0);
    jbyte *szOutBuffer = (jbyte *) (env)->GetByteArrayElements(szOutArray, 0);

    speex_echo_cancellation(m_pState, (spx_int16_t *) recordBuffer,
                            (spx_int16_t *) playBuffer, (spx_int16_t *) szOutBuffer);
    int flag = speex_preprocess_run(m_pPreprocessorState, (spx_int16_t *) szOutBuffer);

    (env)->ReleaseByteArrayElements(recordArray, recordBuffer, 0);
    (env)->ReleaseByteArrayElements(playArray, playBuffer, 0);
    (env)->ReleaseByteArrayElements(szOutArray, szOutBuffer, 0);

    return (1);
}


/* 退出 */
JNIEXPORT jint JNICALL Java_com_gusi_audio_speex_Speex_ExitSpeexDsp(JNIEnv *env, jobject thiz) {
    if (nInitSuccessFlag == 0)
        return (0);

    if (m_pState != NULL) {
        speex_echo_state_destroy(m_pState);
        m_pState = NULL;
    }
    if (m_pPreprocessorState != NULL) {
        speex_preprocess_state_destroy(m_pPreprocessorState);
        m_pPreprocessorState = NULL;
    }

    nInitSuccessFlag = 0;

    return (1);
}


