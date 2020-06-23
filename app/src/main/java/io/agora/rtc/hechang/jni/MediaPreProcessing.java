package io.agora.rtc.hechang.jni;


import java.nio.ByteBuffer;

public class MediaPreProcessing {
    static {
        System.loadLibrary("apm-plugin-raw-data-api");
    }


    public static native void startAudioCustom(boolean isCustomRecord, boolean isCustomPlay);

    public static native void stopAndrelease();
}
