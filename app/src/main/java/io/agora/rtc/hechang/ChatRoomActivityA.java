package io.agora.rtc.hechang;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.hechang.jni.MediaPreProcessing;

import static io.agora.rtc.hechang.Constant.APP_ID;
import static io.agora.rtc.hechang.Constant.TEST_CHANNEL_NAME;

public class ChatRoomActivityA extends AppCompatActivity {
    public static final String TAG = "ChatRoomActivityA";

    private IRtcEngineEventHandler engineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            MediaPreProcessing.startAudioCustom(true, true);
            final String text = "A: Join channel success.\nChannel: " + channel + "\nUid: "
                    + (uid & 0xFFFFFFFFL);
            Log.i(TAG, "onJoinChannelSuccess: " + text);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.text)).setText(text);
                }
            });

            if (mStreamId == 0)
                mStreamId = rtcEngine.createDataStream(false, false);
            rtcEngine.startAudioMixing(Constant.MIXING_PATH, true, false, 2);
        }

        @Override
        public void onStreamMessage(int uid, int streamId, byte[] data) {
            String s = new String(data);
            String s1 = s + rtcEngine.getAudioMixingCurrentPosition();
            if (mStreamId != 0) {
                rtcEngine.sendStreamMessage(mStreamId, s1.getBytes());
            }
        }
    };

    private RtcEngine rtcEngine;
    private volatile int mStreamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        initRtc();
    }

    private void initRtc() {

        try {
            rtcEngine = RtcEngineAgent.create(getApplicationContext(), APP_ID, engineEventHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        rtcEngine.setRecordingAudioFrameParameters(44100, 1, Constants.RAW_AUDIO_FRAME_OP_MODE_WRITE_ONLY, 882);//自采集
        rtcEngine.setPlaybackAudioFrameParameters(44100, 1, Constants.RAW_AUDIO_FRAME_OP_MODE_READ_ONLY, 882);

//        rtcEngine.setParameters("{\"che.audio.external_render\": true}"); //自渲染
        rtcEngine.setParameters("{\"che.audio.opus\": true}");
        rtcEngine.setParameters("{\"rtc.max_playout_delay\":150}");

        rtcEngine.adjustAudioMixingVolume(50);
        rtcEngine.setAudioProfile(0, 3);
        rtcEngine.joinChannel(null, TEST_CHANNEL_NAME, "", 0);
    }

    private void log(String s) {
        Log.i(TAG, s);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaPreProcessing.stopAndrelease();

        if (rtcEngine != null)
            rtcEngine.leaveChannel();

        engineEventHandler = null;
    }
}
