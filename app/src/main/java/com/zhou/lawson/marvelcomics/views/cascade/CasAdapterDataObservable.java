package com.zhou.lawson.marvelcomics.views.cascade;

import android.database.Observable;

/**
 * Created by lawson on 16/2/26.
 */
public class CasAdapterDataObservable extends Observable<CasAdapterDataObserver> {

  public boolean hasObservers() {
    return !mObservers.isEmpty();
  }

  public int getSize() {
    return mObservers.size();
  }

  public void notifyChanged() {
    for (int i = mObservers.size() - 1; i >= 0; i--) {
      mObservers.get(i).onChanged();
    }
  }

  public void notifyItemRangeChanged(int positionStart, int itemCount) {
    for (int i = mObservers.size() - 1; i >= 0; i--) {
      mObservers.get(i).onItemRangeChanged(positionStart, itemCount);
    }
  }

  public void notifyItemRangeInserted(int positionStart, int itemCount) {
    for (int i = mObservers.size() - 1; i >= 0; i--) {
      mObservers.get(i).onItemRangeInserted(positionStart, itemCount);
    }
  }

  public void notifyItemRangeRemoved(int positionStart, int itemCount) {
    for (int i = mObservers.size() - 1; i >= 0; i--) {
      mObservers.get(i).onItemRangeRemoved(positionStart, itemCount);
    }
  }

  public void notifyItemMoved(int fromPosition, int toPosition) {
    for (int i = mObservers.size() - 1; i >= 0; i--) {
      mObservers.get(i).onItemRangeMoved(fromPosition, toPosition, 1);
    }
  }
}
