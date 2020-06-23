package io.agora.rtc.hechang;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import io.agora.rtc.hechang.util.AppUtil;

public class MainActivity extends AppCompatActivity {

    private boolean isHasPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] needPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO};

        isHasPermission = AppUtil.checkAndRequestAppPermission(this, needPermissions, 1);
    }

    public void onClickA(View view) {
        start(ChatRoomActivityA.class);
    }

    public void onClickB(View view) {
        start(ChatRoomActivityB.class);
    }

    private void start(Class<?> activityClass) {
        if (isHasPermission) {
            startActivity(new Intent(this, activityClass));
        } else {
            Toast.makeText(this, "Has no permission.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isHasPermission = false;
                    return;
                }
            }
        }

        isHasPermission = true;
    }

}
