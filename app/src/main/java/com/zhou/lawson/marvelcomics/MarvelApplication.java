package com.zhou.lawson.marvelcomics;

import android.app.Application;
import android.content.Context;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.zhou.lawson.marvelcomics.data.Pool;
import timber.log.Timber;

/**
 * Created by lawson on 16/10/31.
 */
public final class MarvelApplication extends Application {

  private Pool pool;

  @Override public void onCreate() {
    super.onCreate();
    pool = new Pool(this);
    Fresco.initialize(this);

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
  }

  public static MarvelApplication get(Context context) {
    return (MarvelApplication) context.getApplicationContext();
  }

  public Pool getPool() {
    return pool;
  }
}
