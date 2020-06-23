package io.agora.rtc.hechang;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.hechang.jni.MediaPreProcessing;

import static io.agora.rtc.hechang.Constant.APP_ID;
import static io.agora.rtc.hechang.Constant.TEST_CHANNEL_NAME;

public class ChatRoomActivityB extends AppCompatActivity {
    public static final String TAG = "ChatRoomActivityB";

    private IRtcEngineEventHandler engineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            MediaPreProcessing.startAudioCustom(true, true);
            final String text = "B: Join channel success.\nChannel: " + channel + "\nUid: "
                    + (uid & 0xFFFFFFFFL);
            Log.i(TAG, "onJoinChannelSuccess: " + text);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.text)).setText(text);

                    startSyncMsg();
                }
            });
        }

        @Override
        public void onStreamMessage(int uid, int streamId, byte[] data) {
            String s = new String(data);
            String[] s1 = s.split(",");
            if (s1.length != 2)
                return;

            long stampSend = Long.parseLong(s1[0]);
            long stampBack = System.currentTimeMillis();

            int rtt = (int) (stampBack - stampSend);
            log("rtt:" + rtt);

            if (rtt > 300 || rtt < 10)
                return;

            int mixingPos = Integer.parseInt(s1[1]);
            if (mixingPos <= 0) {
                rtcEngine.stopAudioMixing();
                isStartPlay.set(false);
                return;
            }

            if (lastRtt - rtt > 1) {
                if (!isStartPlay.get()) {
                    rtcEngine.startAudioMixing(Constant.MIXING_PATH, true, false, 2);
                    isStartPlay.set(true);
                }
                rtcEngine.setAudioMixingPosition(mixingPos + rtt / 2);
                lastRtt = rtt;
                log("rtt:" + rtt + ", send:" + stampSend + ", back:" + stampBack + "pos:" + mixingPos);
            }
        }
    };

    private RtcEngine rtcEngine;
    private volatile int lastRtt = 400;
    private AtomicBoolean isStartPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);


        isStartPlay = new AtomicBoolean(false);
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
        rtcEngine.setParameters("{\"rtc.max_playout_delay\":120}");
        rtcEngine.setParameters("{\"rtc.min_playout_delay\":120}");
//        rtcEngine.setParameters("{\"che.audio.opensl\":true}");
        rtcEngine.setParameters("{\"che.audio.external_device\":true}");
        rtcEngine.setAudioProfile(0, 3);
        rtcEngine.joinChannel(null, TEST_CHANNEL_NAME, "", 0);
    }

    Handler mHandler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String msg = System.currentTimeMillis() + ",";
            rtcEngine.sendStreamMessage(mStreamId, msg.getBytes());

            mHandler.postDelayed(runnable, 600);
            log("send:" + msg);
        }
    };

    private void log(String s) {
        Log.i(TAG, s);
    }

    private int mStreamId;

    private void startSyncMsg() {
        if (mStreamId == 0)
            mStreamId = rtcEngine.createDataStream(false, false);

        mHandler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaPreProcessing.stopAndrelease();
        if (rtcEngine != null)
            rtcEngine.leaveChannel();

    }
}
