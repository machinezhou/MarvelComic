package com.zhou.lawson.marvelcomics.data.event;

import com.zhou.lawson.marvelcomics.views.Ship;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by lawson on 16/11/16.
 *
 * RxBus should be more extensive, but here only require communication between views
 * instantly. So no annoying things like thread safe or generic event, just prepare a ship and then
 * pull the trigger.
 */
public class Trigger {
  private static volatile Trigger instance;
  /**
   * don't need sticky.
   */
  private final PublishSubject<Observable<Ship>> triggers;

  private Trigger() {
    triggers = PublishSubject.create();
  }

  public static Trigger get() {
    if (instance == null) {
      synchronized (Trigger.class) {
        if (instance == null) {
          instance = new Trigger();
        }
      }
    }
    return instance;
  }

  public void sendShipTrigger(Observable<Ship> ship) {
    triggers.onNext(ship);
  }

  public Observable<Ship> subscribeTrigger() {
    return Observable.switchOnNext(triggers);
  }
}
