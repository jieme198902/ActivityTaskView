package cc.rome753.activitytask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Request overlay window permission
 */
public class RequestOverlayActivity extends Activity {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 0);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && Settings.canDrawOverlays(this)) {
            ActivityTask.start(getApplication());
        }else{
            Toast.makeText(getApplicationContext(), "Window permission denied, cannot show activity task", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
