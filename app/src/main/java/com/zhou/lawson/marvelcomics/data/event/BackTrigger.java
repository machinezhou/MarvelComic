package com.zhou.lawson.marvelcomics.data.event;

import com.zhou.lawson.marvelcomics.views.Ship;
import java.util.Stack;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by lawson on 16/11/16.
 */

public class BackTrigger {

  private static volatile BackTrigger instance;
  /**
   * should not support sticky.
   */
  private final PublishSubject<Observable<Ship>> triggers;
  private final Stack<Action1<Ship>> stack;

  private BackTrigger() {
    triggers = PublishSubject.create();
    stack = new Stack<>();
  }

  public static BackTrigger get() {
    if (instance == null) {
      synchronized (Trigger.class) {
        if (instance == null) {
          instance = new BackTrigger();
        }
      }
    }
    return instance;
  }

  public boolean send(Observable<Ship> ship) {
    if (!stack.isEmpty()) {
      Action1<Ship> action1 = stack.pop();
      Subscription subscription = Observable.switchOnNext(triggers).subscribe(action1);
      triggers.onNext(ship);
      subscription.unsubscribe();
      return true;
    } else {
      return false;
    }
  }

  public void subscribeTrigger(Action1<Ship> action1) {
    if (stack.search(action1) == -1) {
      stack.push(action1);
    }
  }

  public void clearStack() {
    stack.clear();
  }
}
