package com.zhou.lawson.marvelcomics;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.zhou.lawson.marvelcomics.data.event.BackTrigger;
import com.zhou.lawson.marvelcomics.data.event.Trigger;
import com.zhou.lawson.marvelcomics.views.Ship;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements Action1<Ship> {

  public static final String BACK_PRESSED = MainActivity.class.getSimpleName() + "_onBackPressed";
  private static final long CLICK_INTERVAL_TIME = 1500;
  private long lastClickedTime = 0;
  private Subscription subscription;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    subscription = Trigger.get().subscribeTrigger().subscribe(this);
  }

  @Override public void onBackPressed() {
    BackTrigger.get().send(Observable.just(new Ship(BACK_PRESSED)));
  }

  protected void onDoubleBackPressed() {
    long currentTime = SystemClock.elapsedRealtime();
    if ((currentTime - lastClickedTime) >= CLICK_INTERVAL_TIME) {
      Toast.makeText(this, R.string.press_once_again_to_exit, Toast.LENGTH_SHORT).show();
      lastClickedTime = currentTime;
    } else {
      if (subscription != null) {
        subscription.unsubscribe();
      }
      BackTrigger.get().clearStack();
      finish();
    }
  }

  @Override public void call(Ship ship) {
    if (BACK_PRESSED.equals(ship.getTag())) {
      onDoubleBackPressed();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
