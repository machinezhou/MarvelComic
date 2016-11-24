package com.zhou.lawson.marvelcomics.views.single;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.util.DensityUtils;
import java.lang.ref.WeakReference;

/**
 * Created by lawson on 16/11/7.
 */

public class SwitchContainer extends android.widget.ViewAnimator
    implements SwipeRefreshLayout.OnRefreshListener {

  public static final String KEY_CHILD_ID = "current_id";
  public static final String KEY_PARENT_STATE = "parent_state";

  private boolean mAutoRestore;

  private Animation mDefaultInAnimation;
  private Animation mDefaultOutAnimation;

  private long mLastTransitionTime;
  private int mMinShowTime = 200;
  private boolean mPreventFlicker = true;

  /**
   * ensure runnable will be executed before ViewAnimator attaching to window and
   * getHandler() returning null
   */
  private boolean isAttached = false;
  private Runnable mRunnable;

  @BindView(R.id.empty) SwipeRefreshLayout refreshLayout;
  @BindView(R.id.empty_message) TextView emptyMessageView;
  private Context context;
  private Unbinder unbinder;
  private Callback callback;

  public SwitchContainer(Context context, AttributeSet attrs) {
    super(context, attrs);

    final TypedArray typedArray =
        getContext().obtainStyledAttributes(attrs, R.styleable.SwitchContainer);
    mMinShowTime = typedArray.getInt(R.styleable.SwitchContainer_min_show_time, 200);
    mPreventFlicker = typedArray.getBoolean(R.styleable.SwitchContainer_prevent_flicker, true);
    mAutoRestore = typedArray.getBoolean(R.styleable.SwitchContainer_auto_restore, true);
    typedArray.recycle();

    this.context = context;
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public void setEmptyMessage(@NonNull CharSequence emptyMessage) {
    emptyMessageView.setText(emptyMessage);
  }

  public void setEmptyMessage(@StringRes int emptyMessageResId) {
    emptyMessageView.setText(emptyMessageResId);
  }

  public void setRefresh(boolean b) {
    refreshLayout.setRefreshing(b);
  }

  @OnClick(R.id.retry) public void retryClick() {
    if (callback != null) {
      callback.retry();
    }
  }

  @Override public void onRefresh() {
    if (callback != null) {
      refreshLayout.setRefreshing(true);
      callback.refreshNoData();
    }
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    inflate(context, R.layout.layout_switch_wrapper, this);
  }

  private void init() {
    unbinder = ButterKnife.bind(this);
    refreshLayout.setProgressViewOffset(false, 0, 64 * DensityUtils.getDisplayDensity(context));
    refreshLayout.setOnRefreshListener(this);
    refreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.refresh_colors));
  }

  /**
   * Displays the child with the given id performing the specified animations
   *
   * @param inAnimation The animation applied at the entering children
   * @param outAnimation The animation applied at the exiting children
   */
  public void animateTo(@IdRes int childId, @AnimRes int inAnimation, @AnimRes int outAnimation) {
    Animation in = AnimationUtils.loadAnimation(getContext(), inAnimation);
    Animation out = AnimationUtils.loadAnimation(getContext(), outAnimation);
    animateTo(childId, in, out);
  }

  /**
   * Displays the child with the given id performing the specified animations
   *
   * @param inAnimation The animation applied at the entering children
   * @param outAnimation The animation applied at the exiting children
   */
  public void animateTo(@IdRes int childId, Animation inAnimation, Animation outAnimation) {
    setInAnimation(inAnimation);
    setOutAnimation(outAnimation);
    setDisplayedChildId(childId);
  }

  /**
   * Displays the child with the given id performing the default animation
   */
  public void animateTo(@IdRes int childId) {
    setInAnimation(mDefaultInAnimation);
    setOutAnimation(mDefaultOutAnimation);
    setDisplayedChildId(childId);
  }

  /**
   * Displays the child with the given id without performing any animation
   */
  public void moveTo(@IdRes int childId) {
    setInAnimation(null);
    setOutAnimation(null);
    setDisplayedChildId(childId);
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    removeCallbacks(mRunnable);
    mDefaultInAnimation = getInAnimation();
    mDefaultOutAnimation = getOutAnimation();
    init();
    isAttached = true;
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (isAttached) {
      removeCallbacks(mRunnable);
      if (unbinder != null) {
        unbinder.unbind();
      }
    }
    isAttached = false;
  }

  @Override public void setDisplayedChild(final int whichChild) {
    if (!mPreventFlicker) {
      super.setDisplayedChild(whichChild);
      return;
    }
    removeCallbacks(mRunnable);
    mRunnable = new Displayer(this, whichChild);
    int when = mMinShowTime;
    if (System.currentTimeMillis() - mLastTransitionTime > mMinShowTime) {
      when = 0;
    }
    if (!isAttached) {
      mRunnable.run();
    } else {
      postDelayed(mRunnable, when);
    }
  }

  @Override protected Parcelable onSaveInstanceState() {
    Bundle b = new Bundle();
    if (mAutoRestore) {
      b.putInt(KEY_CHILD_ID, getDisplayedChildId());
    }
    b.putParcelable(KEY_PARENT_STATE, super.onSaveInstanceState());
    return b;
  }

  @Override protected void onRestoreInstanceState(Parcelable state) {
    Bundle b = (Bundle) state;
    super.onRestoreInstanceState(b.getParcelable(KEY_PARENT_STATE));
    if (b.containsKey(KEY_CHILD_ID)) {
      moveTo(b.getInt(KEY_CHILD_ID));
    }
  }

  private int getDisplayedChildId() {
    return getChildAt(getDisplayedChild()).getId();
  }

  private void setDisplayedChildId(@IdRes int id) {
    if (getDisplayedChildId() == id) {
      return;
    }
    for (int i = 0, count = getChildCount(); i < count; i++) {
      if (getChildAt(i).getId() == id) {
        setDisplayedChild(i);
        return;
      }
    }
    throw new IllegalArgumentException("No view with ID " + id);
  }

  private void setDisplayedChildInternal(int whichChild) {
    super.setDisplayedChild(whichChild);
  }

  private void setLastTransitionTime(long time) {
    mLastTransitionTime = time;
  }

  private static class Displayer implements Runnable {

    private int mNextState;

    private WeakReference<SwitchContainer> mTargetReference;

    Displayer(SwitchContainer view, int ns) {
      mTargetReference = new WeakReference<>(view);
      mNextState = ns;
    }

    @Override public void run() {
      SwitchContainer target = mTargetReference.get();
      if (target != null && target.getDisplayedChild() != mNextState) {
        target.setDisplayedChildInternal(mNextState);
        target.setLastTransitionTime(System.currentTimeMillis());
      }
    }
  }

  public interface Callback {
    void retry();

    void refreshNoData();
  }
}
