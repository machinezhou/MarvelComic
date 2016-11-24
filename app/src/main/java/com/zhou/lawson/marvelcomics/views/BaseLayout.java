package com.zhou.lawson.marvelcomics.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import com.squareup.picasso.Picasso;
import com.squareup.sqlbrite.BriteDatabase;
import com.zhou.lawson.marvelcomics.MarvelApplication;
import com.zhou.lawson.marvelcomics.data.Pool;
import com.zhou.lawson.marvelcomics.data.event.BackTrigger;
import com.zhou.lawson.marvelcomics.views.loading.LoadingDialog;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by lawson on 16/11/9.
 */

public abstract class BaseLayout extends FrameLayout {

  protected LayoutInflater inflater;
  protected MarvelApplication app;
  protected Pool pool;
  protected Context context;
  protected Picasso picasso;
  protected BriteDatabase database;
  protected Resources resource;
  private LoadingDialog loadingDialog;

  public BaseLayout(Context context) {
    super(context);
    init(context);
  }

  public BaseLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BaseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    inflater = LayoutInflater.from(context);
    app = MarvelApplication.get(context);
    pool = app.getPool();
    resource = getResources();
    picasso = pool.getPicasso();
    database = pool.getDatabase();
  }

  protected void subscribeBackPressed() {
    BackTrigger.get().subscribeTrigger(backPressedAction);
  }

  protected void onBackPressed(Ship ship) {

  }

  protected boolean isDialogLoading() {
    return loadingDialog != null && loadingDialog.isShowing();
  }

  /**
   * show loading dialog
   */
  protected void showLoadingDialog() {
    if (loadingDialog == null) {
      loadingDialog = new LoadingDialog(context);
    }
    if (!loadingDialog.isShowing()) loadingDialog.show();
  }

  /**
   * dismiss loading dialog
   */
  protected void dismissLoadingDialog() {
    if (loadingDialog != null) {
      loadingDialog.dismiss();
    }
  }

  /**
   * show loading dialog action
   */
  protected Action0 showLoadingAction = new Action0() {
    @Override public void call() {
      showLoadingDialog();
    }
  };

  private final Action1<Ship> backPressedAction = new Action1<Ship>() {
    @Override public void call(Ship ship) {
      onBackPressed(ship);
    }
  };
}
