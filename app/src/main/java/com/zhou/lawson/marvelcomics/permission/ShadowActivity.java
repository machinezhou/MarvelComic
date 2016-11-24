package com.zhou.lawson.marvelcomics.permission;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by lawson on 16/9/12.
 */
@TargetApi(Build.VERSION_CODES.M) public class ShadowActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    hideNavigationBar();
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    if (savedInstanceState == null) {
      handleIntent(getIntent());
    }
  }

  @Override protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    String[] permissions = intent.getStringArrayExtra("permissions");
    requestPermissions(permissions, 42);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    RxPermissions.getInstance(this)
        .onRequestPermissionsResult(requestCode, permissions, grantResults);
    finish();
  }

  private void hideNavigationBar() {
    if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
      View v = this.getWindow().getDecorView();
      v.setSystemUiVisibility(View.GONE);
    } else if (Build.VERSION.SDK_INT >= 19) {
      //for new api versions.
      View decorView = getWindow().getDecorView();
      int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
      decorView.setSystemUiVisibility(uiOptions);
    }
  }
}
