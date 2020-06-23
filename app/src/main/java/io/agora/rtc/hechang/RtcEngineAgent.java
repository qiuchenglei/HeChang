package io.agora.rtc.hechang;

import android.content.Context;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class RtcEngineAgent {

    private static IRtcEngineEventHandler rtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            if (handlerFromUser != null) {
                handlerFromUser.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

        @Override
        public void onStreamMessage(int uid, int streamId, byte[] data) {
            if (handlerFromUser != null)
                handlerFromUser.onStreamMessage(uid, streamId, data);
        }
    };

    private static IRtcEngineEventHandler handlerFromUser;

    public static synchronized io.agora.rtc.RtcEngine create(Context context, String appId, IRtcEngineEventHandler handler) throws Exception {
        handlerFromUser = handler;
        return io.agora.rtc.RtcEngine.create(context, appId, rtcEngineEventHandler);
    }
}
