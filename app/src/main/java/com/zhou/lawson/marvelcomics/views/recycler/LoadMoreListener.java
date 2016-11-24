package com.zhou.lawson.marvelcomics.views.recycler;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.squareup.picasso.Picasso;

/**
 * Created by lawson on 16/7/26.
 */
public abstract class LoadMoreListener extends RecyclerView.OnScrollListener {

  private Picasso picasso;
  private String tag;

  public LoadMoreListener() {
  }

  public LoadMoreListener(Picasso picasso, String tag) {
    this.picasso = picasso;
    this.tag = tag;
  }

  @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    int visibleItemsCount = layoutManager.getChildCount();
    int totalItemsCount = layoutManager.getItemCount();
    int firstVisibleItemPos = layoutManager.findFirstVisibleItemPosition();
    int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();

    if (visibleItemsCount == totalItemsCount) {// all items are showed in screen
      return;
    }

    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
      picasso.resumeTag(tag);
      //maybe it is too strict with the variable lastVisibleItemPosition
      if (visibleItemsCount + firstVisibleItemPos >= totalItemsCount
          && lastVisibleItemPosition == totalItemsCount - 1) {
        loadMore();
      }
    } else {
      picasso.pauseTag(tag);
    }
  }

  public abstract void loadMore();
}
