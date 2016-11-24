package com.zhou.lawson.marvelcomics.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.data.database.dealer.ComicDetailModelDealer;
import com.zhou.lawson.marvelcomics.data.event.Trigger;
import com.zhou.lawson.marvelcomics.data.helper.InterceptorSubscriber;
import com.zhou.lawson.marvelcomics.data.models.ComicDetailListModel;
import com.zhou.lawson.marvelcomics.data.models.ComicDetailModel;
import com.zhou.lawson.marvelcomics.data.models.ComicDetailModel_TABLE;
import com.zhou.lawson.marvelcomics.data.models.Thumbnail;
import com.zhou.lawson.marvelcomics.util.EndPoint;
import com.zhou.lawson.marvelcomics.views.refresh.ArcView;
import com.zhou.lawson.marvelcomics.views.refresh.ScrollArcLayout;
import java.util.List;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.zhou.lawson.marvelcomics.util.CheckUtils.isEmpty;

/**
 * Created by lawson on 16/11/12.
 */
public class ComicDetailLayout extends BaseLayout implements ArcView.OnRefreshListener {

  public static final String TAG = ComicDetailLayout.class.getSimpleName() + "_switch";
  @BindView(R.id.background) ImageView backgroundView;
  @BindView(R.id.scroll_layout) ScrollArcLayout scrollLayout;

  private Ship ship;
  private final CompositeSubscription subscription = new CompositeSubscription();
  private Unbinder unbinder;
  private ComicDetailModelDealer dealer;

  public ComicDetailLayout(Context context, Ship ship) {
    this(context);
    this.ship = ship;
  }

  public ComicDetailLayout(Context context) {
    super(context);
  }

  public ComicDetailLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ComicDetailLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (getChildCount() == 0) {
      inflate(getContext(), R.layout.layout_comic_detail, this);
      unbinder = ButterKnife.bind(this);
      dealer = new ComicDetailModelDealer(pool.getGson());
      subscribeBackPressed();
      refillData();
    }
  }

  private void refillData() {
    loadLocalData();
    loadData(false);
  }

  private void loadLocalData() {
    subscription.add(database.createQuery(ComicDetailModel_TABLE.TABLE_NAME,
        ComicDetailModel_TABLE.TABLE_BASE_QUERY)
        .map(dealer.MAP)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<List<ComicDetailModel>>() {
          @Override public void call(List<ComicDetailModel> comicDetailModels) {
            if (!isEmpty(comicDetailModels)) {
              handleDetail(comicDetailModels.get(0));
            }
          }
        }));
  }

  @Override public void refresh() {
    loadData(true);
  }

  private void loadData(final boolean fromRefresh) {
    subscription.add(
        pool.getAllProvider()
            .getComicDetail(ship.getInt("comic_id"))
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                if (!fromRefresh) {
                  showLoadingDialog();
                }
              }
            })
            .compose(dealer.saveToDatabase(true, database))
            .subscribe(new InterceptorSubscriber<ComicDetailListModel>(app) {
              @Override public void onEnd() {
                scrollLayout.setRefreshing(false);
                dismissLoadingDialog();
              }
            }));
  }

  void handleDetail(ComicDetailModel model) {
    Thumbnail thumbnail = model.thumbnail;
    picasso.load(
        thumbnail == null ? EndPoint.badUrl() : (thumbnail.path + "." + thumbnail.extension))
        .placeholder(R.drawable.ironman)
        .error(R.drawable.ironman)
        .into(backgroundView);
    scrollLayout.fillData(model, this);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (ship != null) {
      ship.clear();
    }
    if (unbinder != null) {
      unbinder.unbind();
    }
    if (subscription != null) {
      subscription.unsubscribe();
    }
  }

  @Override protected void onBackPressed(Ship ship) {
    Trigger.get().sendShipTrigger(Observable.just(new Ship(TAG)));
  }
}
