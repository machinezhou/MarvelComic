package com.zhou.lawson.marvelcomics.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.data.database.dealer.CharacterModelDealer;
import com.zhou.lawson.marvelcomics.data.event.Trigger;
import com.zhou.lawson.marvelcomics.data.helper.InterceptorSubscriber;
import com.zhou.lawson.marvelcomics.data.models.CharacterListModel;
import com.zhou.lawson.marvelcomics.data.models.CharacterModel;
import com.zhou.lawson.marvelcomics.data.models.CharacterModel_TABLE;
import com.zhou.lawson.marvelcomics.data.models.Thumbnail;
import com.zhou.lawson.marvelcomics.util.EndPoint;
import com.zhou.lawson.marvelcomics.views.cascade.CascadeView;
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
 * Created by lawson on 16/11/15.
 */

public class CharactersLayout extends BaseLayout implements SwitchContainer.Callback {

  public static final String TAG = CharactersLayout.class.getSimpleName();
  private final CompositeSubscription subscription = new CompositeSubscription();

  public final static int LIST_LIMIT = 20;

  @BindView(R.id.character_list) CascadeView charactersView;
  @BindView(R.id.switch_container) SwitchContainer switchContainer;
  private Unbinder unbinder;

  private int currentOffset;
  private boolean isRefresh = true;
  private boolean isAttached = false;
  private CharacterAdapter adapter;
  private CharacterModelDealer dealer;

  public CharactersLayout(Context context) {
    super(context);
  }

  public CharactersLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CharactersLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (getChildCount() == 0) {
      inflate(context, R.layout.layout_characters, this);
      unbinder = ButterKnife.bind(this);
      init();
    } else {
      unbinder = ButterKnife.bind(this);
    }
    isAttached = true;
  }

  private void init() {
    switchContainer.setCallback(this);
    adapter = new CharacterAdapter();
    charactersView.setAdapter(adapter);
    charactersView.setOnSwipeToLastListener(onSwipeListener);
    charactersView.setDisallowIntercept(true);
    charactersView.setOnCoverClickListener(coverClickListener);
    dealer = new CharacterModelDealer(pool.getGson());

    loadLocalData();
    loadData();
  }

  private void loadLocalData() {
    subscription.add(
        database.createQuery(CharacterModel_TABLE.TABLE_NAME, CharacterModel_TABLE.TABLE_BASE_QUERY)
            .map(dealer.MAP)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(adapter));
  }

  private void loadData() {
    currentOffset = 0;
    isRefresh = true;
    subscription.add(
        pool.getAllProvider()
            .getCharacters(currentOffset)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                showLoadingDialog();
                switchContainer.animateTo(R.id.content);
              }
            })
            .compose(dealer.saveToDatabase(isRefresh, database))
            .subscribe(new InterceptorSubscriber<CharacterListModel>(app) {
              @Override public void onEnd() {
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
      isRefresh = false;
      subscription.add(pool.getAllProvider()
          .getCharacters(currentOffset + LIST_LIMIT)
          .doOnSubscribe(showLoadingAction)
          .compose(dealer.saveToDatabase(isRefresh, database))
          .subscribe(new InterceptorSubscriber<CharacterListModel>(app) {
            @Override public void onEnd() {
              dismissLoadingDialog();
            }
          }));
    }
  }

  public class CharacterAdapter extends CascadeView.CasAdapter<CharacterViewHolder>
      implements Action1<List<CharacterModel>> {

    private List<CharacterModel> list = Collections.emptyList();

    public CharacterAdapter() {
    }

    void setItems(List<CharacterModel> l) {
      list = l;
      notifyDataSetChanged();
    }

    void addItems(List<CharacterModel> l) {
      list.addAll(l);
      notifyItemRangeInserted(getItemCount() + LIST_LIMIT, LIST_LIMIT);
    }

    int getItemId(int position) {
      return list.get(position).id;
    }

    @Override public CharacterViewHolder onCreateView() {
      return new CharacterViewHolder(
          inflater.inflate(R.layout.layout_item_characters, null, false));
    }

    @Override public int getItemCount() {
      return list.size();
    }

    @Override public void onBindViewHolder(CharacterViewHolder holder, int position) {
      holder.bindList(list.get(position));
    }

    @Override public void call(List<CharacterModel> l) {
      if (isRefresh) {
        setItems(l);
        if (getItemCount() > 0) {
          switchContainer.animateTo(R.id.content);
        } else {
          switchContainer.animateTo(R.id.empty);
        }
      } else {
        List<CharacterModel> temp = l.subList(currentOffset + LIST_LIMIT, l.size() - 1);
        addItems(temp);
        if (isEmpty(l)) {
          currentOffset = currentOffset + LIST_LIMIT;
        }
      }
    }
  }

  class CharacterViewHolder extends CascadeView.ViewHolder {

    @BindView(R.id.thumbnail) ImageView thumbnailView;
    @BindView(R.id.name) TypefaceTextView nameView;

    public CharacterViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    void bindList(final CharacterModel model) {
      nameView.setText(model.name);
      Thumbnail thumbnail = model.thumbnail;
      picasso.load(
          thumbnail == null ? EndPoint.badUrl() : (thumbnail.path + "." + thumbnail.extension))
          .into(thumbnailView);
    }
  }

  private final CascadeView.OnCoverClickListener coverClickListener =
      new CascadeView.OnCoverClickListener() {
        @Override public void onCoverClickListener() {
          final Ship ship = new Ship(TAG);
          ship.putInt("character_id", adapter.getItemId(charactersView.getCoverPositionInItems()));
          Trigger.get().sendShipTrigger(Observable.just(ship));
        }
      };

  final CascadeView.OnSwipeListener onSwipeListener = new CascadeView.OnSwipeListener() {
    @Override public void onSwipeToLast() {
      moreData();
    }

    @Override public void onSwipeToCover() {
      loadData();
    }
  };

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (isAttached) {
      if (unbinder != null) {
        unbinder.unbind();
      }
      subscription.clear();
    }
    isAttached = false;
  }

  @Override public void retry() {
    loadData();
  }

  @Override public void refreshNoData() {
    loadData();
  }
}
