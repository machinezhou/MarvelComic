package com.zhou.lawson.marvelcomics.views.cascade;

/**
 * Created by lawson on 16/2/26.
 */
public abstract class CasAdapterDataObserver {
  public void onChanged() {
  }

  public void onItemRangeChanged(int positionStart, int itemCount) {
  }

  public void onItemRangeInserted(int positionStart, int itemCount) {
  }

  public void onItemRangeRemoved(int positionStart, int itemCount) {
  }

  public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
  }
}
