package com.zhou.lawson.marvelcomics.data.helper.rx;

import rx.Observer;

/**
 * Created by lawson on 16/6/25.
 */
public abstract class EndObserver implements Observer {
  @Override public void onCompleted() {
    onEnd();
  }

  @Override public void onError(Throwable e) {
    onEnd();
  }

  public abstract void onEnd();
}
