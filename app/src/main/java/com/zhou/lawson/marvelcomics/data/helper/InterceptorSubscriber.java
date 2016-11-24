package com.zhou.lawson.marvelcomics.data.helper;

import android.widget.Toast;
import com.zhou.lawson.marvelcomics.MarvelApplication;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.data.helper.rx.EndSubscriber;
import com.zhou.lawson.marvelcomics.data.models.BaseModel;
import timber.log.Timber;

/**
 * Created by lawson on 16/11/3.
 */

public abstract class InterceptorSubscriber<T extends BaseModel> extends EndSubscriber<T> {

  private final MarvelApplication app;
  private boolean shouldToast = true; //should this response toast out or not

  public InterceptorSubscriber(MarvelApplication app) {
    this(app, true);
  }

  public InterceptorSubscriber(MarvelApplication app, boolean shouldToast) {
    this.app = app;
    this.shouldToast = shouldToast;
  }

  @Override public void onNext(T t) {
    if (t != null) {
      if (t.isSucceed()) {
        onCleanSuccess(t);
      } else {
        onError(new Throwable("failed code : " + t.code + ", status : " + t.status));
      }
    } else {
      onError(new Throwable("gson converting failed"));
    }
  }

  @Override public void onError(Throwable throwable) {
    super.onError(throwable);
    if (throwable instanceof RetrofitException) {
      RetrofitException cause = (RetrofitException) throwable;
      RetrofitException.Kind kind = cause.getKind();
      switch (kind) {
        case HTTP:
          if (cause.getResponse().code() != 304) {
            Toast.makeText(app, R.string.http_exception, Toast.LENGTH_SHORT).show();
          } else {
            //not pass 304 as error to avoid code checking at every response
            return;
          }
          break;
        case NETWORK:
          Toast.makeText(app, R.string.no_connection, Toast.LENGTH_SHORT).show();
          break;
        case UNEXPECTED:
          Toast.makeText(app, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
          break;
        default:
          break;
      }
    } else {
      Timber.e(throwable, "error occurred.", "unknown");
    }
    onCleanError(throwable);
  }

  public void onCleanError(Throwable throwable) {
  }

  public void onCleanSuccess(T t) {

  }
}
