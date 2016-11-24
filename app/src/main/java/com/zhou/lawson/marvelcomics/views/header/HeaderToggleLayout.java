package com.zhou.lawson.marvelcomics.views.header;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.data.event.BackTrigger;
import com.zhou.lawson.marvelcomics.data.event.Trigger;
import com.zhou.lawson.marvelcomics.views.Ship;
import rx.Observable;

/**
 * Created by lawson on 16/11/4.
 */

public class HeaderToggleLayout extends CardView {

  public static final String HEADER_BACK_PRESSED =
      HeaderToggleLayout.class.getSimpleName() + "_onBackPressed";
  public static final String TAG = HeaderToggleLayout.class.getSimpleName() + "_toggle";

  private Unbinder unbinder;
  private Context context;
  private HeaderToggleDrawable toggleDrawable;
  @BindView(R.id.left) TextView leftView;
  @BindView(R.id.title) TextView titleView;
  @BindView(R.id.right) TextView rightView;
  private boolean isAttached = false;

  public HeaderToggleLayout(Context context) {
    this(context, null);
  }

  public HeaderToggleLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public HeaderToggleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.context = context;

    int primaryColor = ContextCompat.getColor(context, R.color.darkBlue);
    int toggleColor = ContextCompat.getColor(context, R.color.darkRed);
    setBackgroundColor(primaryColor);
    toggleDrawable = new HeaderToggleDrawable(toggleColor);
    setClickable(true);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (getChildCount() == 0) {
      inflate(context, R.layout.layout_header, this);
      unbinder = ButterKnife.bind(this);
      init();
    }
    isAttached = true;
  }

  private void init() {
    showLeft(VISIBLE);
    setTitle(R.string.app_name);
    leftView.setBackground(toggleDrawable);
  }

  @OnClick(R.id.left) public void toggle() {
    if (toggleDrawable.getOrientation() == HeaderToggleDrawable.DOT_ORIENTATION_LEFT) {
      BackTrigger.get().send(Observable.just(new Ship(HEADER_BACK_PRESSED)));
    } else {
      Trigger.get().sendShipTrigger(Observable.just(new Ship(TAG)));
    }
  }

  public void setHeaderOrientation(@DotOrientation int orientation) {
    toggleDrawable.toggle(orientation);
  }

  public int getHeaderOrientation() {
    return toggleDrawable.getOrientation();
  }

  public void setTitle(CharSequence title) {
    titleView.setText(title);
  }

  public void setTitle(@StringRes int resId) {
    titleView.setText(resId);
  }

  public void showLeft(int visible) {
    leftView.setVisibility(visible);
  }

  public void showRight(int visible) {
    rightView.setVisibility(visible);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (isAttached && unbinder != null) {
      unbinder.unbind();
    }
    isAttached = false;
  }
}
