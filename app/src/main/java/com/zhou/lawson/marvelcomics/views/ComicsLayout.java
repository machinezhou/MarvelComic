package com.zhou.lawson.marvelcomics.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.data.database.dealer.ComicModelDealer;
import com.zhou.lawson.marvelcomics.data.event.Trigger;
import com.zhou.lawson.marvelcomics.data.helper.InterceptorSubscriber;
import com.zhou.lawson.marvelcomics.data.models.ComicListModel;
import com.zhou.lawson.marvelcomics.data.models.ComicModel;
import com.zhou.lawson.marvelcomics.data.models.ComicModel_TABLE;
import com.zhou.lawson.marvelcomics.data.models.Thumbnail;
import com.zhou.lawson.marvelcomics.util.EndPoint;
import com.zhou.lawson.marvelcomics.views.recycler.LoadMoreListener;
import com.zhou.lawson.marvelcomics.views.recycler.RecyclerClickListener;
import com.zhou.lawson.marvelcomics.views.recycler.SpaceLayoutDecoration;
import com.zhou.lawson.marvelcomics.views.single.SwitchContainer;
import com.zhou.lawson.marvelcomics.views.single.TypefaceTextView;
import java.util.Collections;
import java.util.List;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.zhou.lawson.marvelcomics.util.CheckUtils.isEmpty;

/**
 * Created by lawson on 16/11/7.
 */

public class ComicsLayout extends BaseLayout
    implements SwipeRefreshLayout.OnRefreshListener, SwitchContainer.Callback {

  public static final String TAG = ComicsLayout.class.getSimpleName();

  private final CompositeSubscription subscription = new CompositeSubscription();
  public final static int LIST_LIMIT = 20;

  @BindView(R.id.comic_list) RecyclerView comicList;
  @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;
  @BindView(R.id.switch_container) SwitchContainer switchContainer;
  @BindDrawable(R.drawable.layout_item_indicator) Drawable itemIndicator;
  @BindDimen(R.dimen.small_space) int smallSpace;

  private Unbinder unbinder;
  private ComicsAdapter adapter;
  private ComicModelDealer dealer;
  private int currentOffset = 0;
  private boolean isRefresh = true;
  private boolean isAttached = false;

  public ComicsLayout(Context context) {
    super(context);
  }

  public ComicsLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ComicsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (getChildCount() == 0) {
      inflate(context, R.layout.layout_comics, this);
      unbinder = ButterKnife.bind(this);
      init();
    } else {
      unbinder = ButterKnife.bind(this);
    }
    isAttached = true;
  }

  private void init() {
    switchContainer.setCallback(this);
    refreshLayout.setOnRefreshListener(this);
    refreshLayout.setColorSchemeColors(resource.getIntArray(R.array.refresh_colors));

    adapter = new ComicsAdapter(itemClickListener);
    comicList.setAdapter(adapter);
    comicList.setLayoutManager(new LinearLayoutManager(context));
    comicList.addItemDecoration(
        new SpaceLayoutDecoration.Builder(SpaceLayoutDecoration.LINEAR_LAYOUT_VERTICAL).linearSpace(
            smallSpace)
            .headCut(smallSpace)
            .tailCut(smallSpace)
            .leftCut(smallSpace)
            .rightCut(smallSpace)
            .build());
    comicList.addOnScrollListener(loadMoreListener);
    dealer = new ComicModelDealer(pool.getGson());

    loadLocalData();
    loadData(false);
  }

  private void loadLocalData() {
    subscription.add(
        database.createQuery(ComicModel_TABLE.TABLE_NAME, ComicModel_TABLE.TABLE_BASE_QUERY)
            .map(dealer.MAP)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(adapter));
  }

  private void loadData(final boolean fromRefresh) {
    currentOffset = 0;
    isRefresh = true;
    subscription.add(pool.getAllProvider()
        .getComics(currentOffset)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            if (!fromRefresh) {
              showLoadingDialog();
            }
            switchContainer.animateTo(R.id.content);
          }
        })
        .compose(dealer.saveToDatabase(isRefresh, database))
        .subscribe(new InterceptorSubscriber<ComicListModel>(app) {
          @Override public void onEnd() {
            refreshLayout.setRefreshing(false);
            switchContainer.setRefresh(false);
            dismissLoadingDialog();
          }

          @Override public void onCleanError(Throwable throwable) {
            super.onCleanError(throwable);
            if (adapter.getItemCount() == 0) {
              switchContainer.animateTo(R.id.loading_failed);
            } else {
              switchContainer.animateTo(R.id.content);
            }
          }
        }));
  }

  void moreData() {
    if (!isDialogLoading()) {
      final int offset = currentOffset + LIST_LIMIT;
      isRefresh = false;
      subscription.add(pool.getAllProvider()
          .getComics(offset)
          .doOnSubscribe(showLoadingAction)
          .compose(dealer.saveToDatabase(isRefresh, database))
          .subscribe(new InterceptorSubscriber<ComicListModel>(app) {

            @Override public void onEnd() {
              refreshLayout.setRefreshing(false);
              dismissLoadingDialog();
            }
          }));
    }
  }

  public final class ComicsAdapter extends RecyclerView.Adapter<ComicsAdapter.ComicsViewHolder>
      implements Action1<List<ComicModel>> {

    private final RecyclerClickListener mRecyclerListener;
    private List<ComicModel> list = Collections.emptyList();

    ComicsAdapter(RecyclerClickListener mRecyclerListener) {
      this.mRecyclerListener = mRecyclerListener;
    }

    void setItems(List<ComicModel> l) {
      list = l;
      notifyDataSetChanged();
    }

    void addItems(List<ComicModel> l) {
      list.addAll(l);
      notifyItemRangeInserted(getItemCount() + LIST_LIMIT, LIST_LIMIT);
    }

    int getComicId(int position) {
      return list.get(position).id;
    }

    @Override public ComicsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new ComicsViewHolder(inflater.inflate(R.layout.layout_item_comics, parent, false),
          mRecyclerListener);
    }

    @Override public void onBindViewHolder(ComicsViewHolder holder, int position) {
      holder.bindList(list.get(position));
    }

    @Override public int getItemCount() {
      return list.size();
    }

    @Override public void call(List<ComicModel> l) {
      if (isRefresh) {
        setItems(l);
        if (getItemCount() > 0) {
          switchContainer.animateTo(R.id.content);
        } else {
          switchContainer.animateTo(R.id.empty);
        }
      } else {
        List<ComicModel> temp = l.subList(currentOffset + LIST_LIMIT, l.size() - 1);
        addItems(temp);
        if (isEmpty(l)) {
          currentOffset = currentOffset + LIST_LIMIT;
        }
      }
    }

    public class ComicsViewHolder extends RecyclerView.ViewHolder {

      @BindView(R.id.thumbnail) ImageView thumbnailView;
      @BindView(R.id.title) TypefaceTextView titleView;

      public ComicsViewHolder(View itemView, final RecyclerClickListener recyclerClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        bindListener(itemView, recyclerClickListener);
      }

      void bindList(final ComicModel model) {
        titleView.setText(model.title);
        Thumbnail thumbnail = model.thumbnail;
        picasso.load(
            thumbnail == null ? EndPoint.badUrl() : (thumbnail.path + "." + thumbnail.extension))
            .tag(TAG)
            .into(thumbnailView);
      }

      private void bindListener(View itemView, final RecyclerClickListener recyclerClickListener) {
        itemView.setOnClickListener(new OnClickListener() {
          @Override public void onClick(View v) {
            recyclerClickListener.onElementClick(getAdapterPosition());
          }
        });
      }
    }
  }

  private final RecyclerClickListener itemClickListener = new RecyclerClickListener() {
    @Override public void onElementClick(int position) {
      final Ship ship = new Ship(TAG);
      ship.putInt("comic_id", adapter.getComicId(position));
      Trigger.get().sendShipTrigger(Observable.just(ship));
    }
  };

  private final LoadMoreListener loadMoreListener = new LoadMoreListener(picasso, TAG) {
    @Override public void loadMore() {
      moreData();
    }
  };

  @Override public void onRefresh() {
    refreshLayout.setRefreshing(true);
    loadData(true);
  }

  @Override public void retry() {
    loadData(false);
  }

  @Override public void refreshNoData() {
    loadData(true);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    cleanup();
  }

  private void cleanup() {
    if (isAttached) {
      if (unbinder != null) {
        unbinder.unbind();
      }
      subscription.clear();
      if (comicList != null) {
        comicList.removeOnScrollListener(loadMoreListener);
      }
      picasso.cancelTag(TAG);
    }
    isAttached = false;
  }
}
