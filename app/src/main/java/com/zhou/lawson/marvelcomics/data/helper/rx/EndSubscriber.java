package com.zhou.lawson.marvelcomics.data.helper.rx;

import rx.Subscriber;

public abstract class EndSubscriber<T> extends Subscriber<T>{
  @Override public void onCompleted() {
    onEnd();
  }

  @Override public void onError(Throwable throwable) {
    onEnd();
  }

  public abstract void onEnd();
}
