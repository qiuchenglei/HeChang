#include <jni.h>
#include <android/log.h>
#include <cstring>
#include "include/IAgoraMediaEngine.h"

#include "include/IAgoraRtcEngine.h"
#include <string.h>
#include "media_preprocessing_plugin_jni.h"
#include "include/VMUtil.h"

#include <map>

#define LOG(...) __android_log_print(ANDROID_LOG_DEBUG,"dev_mars",__VA_ARGS__)

using namespace std;

class AgoraAudioFrameObserver : public agora::media::IAudioFrameObserver {

public:
    AgoraAudioFrameObserver() {
    }

    ~AgoraAudioFrameObserver() {
    }

public:
    virtual bool onRecordAudioFrame(AudioFrame &audioFrame) override {
        LOG("onRecordAudioFrame");
        // todo 通过替换 audioFrame.buffer 的数据来自采集，长度不变
        return true;
    }

    virtual bool onPlaybackAudioFrame(AudioFrame &audioFrame) override {
        LOG("onPlaybackAudioFrame");
        // todo 播放 audioFrame.buffer
        return true;
    }

    virtual bool
    onPlaybackAudioFrameBeforeMixing(unsigned int uid, AudioFrame &audioFrame) override {
        return true;
    }

    virtual bool onMixedAudioFrame(AudioFrame &audioFrame) override {
        return true;
    }
};


static AgoraAudioFrameObserver s_audioFrameObserver;
static agora::rtc::IRtcEngine *rtcEngine = nullptr;

#ifdef __cplusplus
extern "C" {
#endif

int __attribute__((visibility("default")))
loadAgoraRtcEnginePlugin(agora::rtc::IRtcEngine *engine) {
    __android_log_print(ANDROID_LOG_DEBUG, "agora-raw-data-plugin", "loadAgoraRtcEnginePlugin");
    rtcEngine = engine;
    return 0;
}

void __attribute__((visibility("default")))
unloadAgoraRtcEnginePlugin(agora::rtc::IRtcEngine *engine) {
    __android_log_print(ANDROID_LOG_DEBUG, "agora-raw-data-plugin", "unloadAgoraRtcEnginePlugin");

    rtcEngine = nullptr;
}

JNIEXPORT void JNICALL
Java_io_agora_rtc_hechang_jni_MediaPreProcessing_startAudioCustom(JNIEnv *env, jclass type,
                                                                  jboolean isCustomRecord,
                                                                  jboolean isCustomPlay) {

    agora::util::AutoPtr<agora::media::IMediaEngine> mediaEngine;
    mediaEngine.queryInterface(rtcEngine, agora::INTERFACE_ID_TYPE::AGORA_IID_MEDIA_ENGINE);
    if (mediaEngine) {
        mediaEngine->registerAudioFrameObserver(&s_audioFrameObserver);
    }

    if (isCustomRecord) {
        //todo init and start record service
    }

    if (isCustomPlay) {
        //todo init and start play service
    }
}

JNIEXPORT void JNICALL
Java_io_agora_rtc_hechang_jni_MediaPreProcessing_stopAndrelease(JNIEnv *env, jclass type) {
    agora::util::AutoPtr<agora::media::IMediaEngine> mediaEngine;
    mediaEngine.queryInterface(rtcEngine, agora::INTERFACE_ID_TYPE::AGORA_IID_MEDIA_ENGINE);

    if (mediaEngine) {
        mediaEngine->registerAudioFrameObserver(NULL);
    }
}

#ifdef __cplusplus
}
#endif