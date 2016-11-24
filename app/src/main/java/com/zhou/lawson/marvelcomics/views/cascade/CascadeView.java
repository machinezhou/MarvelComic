package com.zhou.lawson.marvelcomics.views.cascade;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lawson on 16/2/14.
 *
 * usage :
 * <code>
 * class SampleCasAdapter extends CascadeView.CasAdapter<SampleViewHolder>{
 * ...
 * }
 *
 * class SampleViewHolder extends CascadeView.ViewHolder{
 * ...
 * }
 * <code/>
 */
//// FIXME: 16/11/1 refactor
public class CascadeView extends FrameLayout {

  private static final int DEFAULT_COUNT = 2;
  private static final int COVER_POSITION_IN_CACHE = 0;

  private float ratio = 0.4f;

  private final List<ViewHolder> cache = new ArrayList<>();
  private final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();

  private int visibleViewCount = DEFAULT_COUNT;
  private int coverPositionInItems;
  private boolean mRunPredictiveAnimations = false;
  private boolean disallowIntercept = false;
  private int mTouchSlop;
  private boolean mIsBeingDragged;

  private int initChildLeft;
  private int initChildRight;
  private int initChildTop;
  private int initChildBottom;
  private float lastY;

  private CasAdapter adapter;
  private OnSwipeListener swipeListener;
  private OnCoverClickListener coverClickListener;

  public CascadeView(Context context) {
    this(context, null);
  }

  public CascadeView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CascadeView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
  }

  public void setAdapter(CasAdapter casAdapter) {
    if (adapter != null) {
      adapter.unregisterAdapterDataObserver(mObserver);
    }

    adapter = casAdapter;

    if (adapter == null) {
      throw new RuntimeException("adapter must not be null");
    } else {
      adapter.registerAdapterDataObserver(mObserver);
    }

    refresh();
  }

  private void refresh() {
    clear();

    coverPositionInItems = 0;

    if (adapter.getItemCount() > 0) {
      for (int i = 0; i < visibleViewCount; i++) {
        ViewHolder vh = adapter.createView();
        add(vh, i);
      }
    }
  }

  public void setOnSwipeToLastListener(OnSwipeListener listener) {
    swipeListener = listener;
  }

  public void setDisallowIntercept(boolean disallow) {
    disallowIntercept = disallow;
  }

  private void add(ViewHolder holder, int position) {
    adapter.bindViewHolder(holder, position);
    cache.add(holder);
    addView(holder.itemView, position);
    cache.get(COVER_POSITION_IN_CACHE).itemView.bringToFront();
    remeasureChildren();
  }

  private void addToLast(ViewHolder holder) {
    coverPositionInItems++;
    adapter.bindViewHolder(holder, coverPositionInItems + 1);
    cache.add(holder);
    addView(holder.itemView, visibleViewCount - 1);
    cache.get(COVER_POSITION_IN_CACHE).itemView.bringToFront();
    remeasureChildren();
  }

  private void addToCover(ViewHolder holder) {
    coverPositionInItems--;
    adapter.bindViewHolder(holder, coverPositionInItems);
    cache.add(COVER_POSITION_IN_CACHE, holder);
    addView(holder.itemView);
    remeasureChildren();
    finishLastMoving(holder.itemView);
  }

  private void remove(ViewHolder holder, int holderPosition) {
    cache.remove(holderPosition);
    removeView(holder.itemView);
  }

  private void removeCover() {
    ViewHolder holder = getCoverViewHolder();
    remove(holder, COVER_POSITION_IN_CACHE);
    if (scrollToLast(coverPositionInItems, adapter.getItemCount())) {
      mAttachedScrap.add(holder);
      swipeListener.onSwipeToLast();
    } else {
      addToLast(holder);
    }
  }

  private void removeLast() {
    if (scrollToCover()) {
      swipeListener.onSwipeToCover();
    } else {
      ViewHolder holder = getLastViewHolder();
      remove(holder, visibleViewCount - 1);
      addToCover(holder);
    }
  }

  private boolean scrollToLast(int position, int size) {
    if (position >= size) {
      throw new IndexOutOfBoundsException("Invalid index " + position + ", size is " + size);
    }
    return coverPositionInItems == (size - 1) - 1;
  }

  private boolean scrollToCover() {
    return coverPositionInItems == 0;
  }

  private boolean scrollToTopEdge(float d) {
    return d < 0 && Math.abs(d) > getCoverViewHolder().itemView.getPaddingTop();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int l = getPaddingLeft();
    int t = getPaddingTop();
    int r = getPaddingRight();
    int b = getPaddingBottom();

    int size = getChildCount();
    for (int i = 0; i < size; i++) {
      View view = getChildAt(i);
      int childWidth = getMeasuredWidth() - l - r;
      int childHeight = getMeasuredHeight() - t - b;

      view.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY));
    }
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    int count = getChildCount();

    int l = getPaddingLeft();
    int t = getPaddingTop();
    int r = getPaddingRight();
    int b = getPaddingBottom();
    for (int i = 0; i < count; i++) {
      View child = getChildAt(i);

      child.layout(l, t, getMeasuredWidth() - r, getMeasuredHeight() - b);
      if (i == count - 1) {
        initChildLeft = l;
        initChildRight = getMeasuredWidth() - r;
        initChildTop = t;
        initChildBottom = getMeasuredHeight() - b;
      }
    }
  }

  private void remeasureChildren() {
    int count = getChildCount();

    if (count > 0) {
      int l = getPaddingLeft();
      int t = getPaddingTop();
      int r = getPaddingRight();
      int b = getPaddingBottom();
      initChildLeft = l;
      initChildRight = getMeasuredWidth() - r;
      initChildTop = t;
      initChildBottom = getMeasuredHeight() - b;
    }
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    requestDisallowInterceptTouchEvent(disallowIntercept);
    return super.onInterceptTouchEvent(ev);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    if (getChildCount() < visibleViewCount || mRunPredictiveAnimations) {
      return true;
    }

    int action = event.getAction();
    float y = event.getY();
    float delta = y - lastY;
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        lastY = y;
        mIsBeingDragged = false;
        break;
      case MotionEvent.ACTION_MOVE:
        mIsBeingDragged = Math.abs(delta) > mTouchSlop;
        if (mIsBeingDragged) {
          if (scrollToTopEdge(delta)) {
            return true;
          } else {
            moveCover((int) delta);
          }
        }
        break;
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        if (mIsBeingDragged) {
          if (scrollToTopEdge(delta)) {
            removeLast();
          } else {
            finishCoverMoving(getCoverViewHolder().itemView);
          }
        } else {
          if (coverClickListener != null) coverClickListener.onCoverClickListener();
        }
        mIsBeingDragged = false;
    }
    return true;
  }

  private void moveCover(int distance) {
    ViewHolder holder = getCoverViewHolder();
    distance *= ratio;
    holder.itemView.layout(initChildLeft, initChildTop + distance, initChildRight,
        initChildBottom + distance);
  }

  private void finishCoverMoving(View v) {
    v.startAnimation(new MoveAnimation(v, 0, 0, v.getTop(), initChildBottom, false));
  }

  private void finishLastMoving(View v) {
    v.startAnimation(new MoveAnimation(v, 0, 0, initChildBottom, initChildTop, true));
  }

  private ViewHolder getViewHolderForPosition(int position) {
    if (cache == null || cache.isEmpty()) {
      throw new RuntimeException("cache size is empty");
    }
    if (position < 0 || position >= visibleViewCount) {
      throw new IndexOutOfBoundsException(
          "Invalid index " + position + ", size is " + visibleViewCount);
    }
    return cache.get(position);
  }

  private ViewHolder getCoverViewHolder() {
    return getViewHolderForPosition(COVER_POSITION_IN_CACHE);
  }

  private ViewHolder getLastViewHolder() {
    return getViewHolderForPosition(visibleViewCount - 1);
  }

  private void clear() {
    cache.clear();
    mAttachedScrap.clear();
    removeAllViews();
  }

  public CasAdapter getAdapter() {
    return adapter;
  }

  public int getCoverPositionInItems() {
    return coverPositionInItems;
  }

  public void setOnCoverClickListener(OnCoverClickListener listener) {
    coverClickListener = listener;
  }

  @Override public void setOnClickListener(OnClickListener l) {
    throw new IllegalArgumentException("OnClickListener from outside is not supported.");
  }

  public abstract static class OnSwipeListener {
    public void onSwipeToLast() {
    }

    public void onSwipeToCover() {

    }
  }

  public abstract static class OnCoverClickListener {
    public void onCoverClickListener() {

    }
  }

  public class MoveAnimation extends AnimationSet implements Animation.AnimationListener {
    private final View view;
    private final boolean up;
    private final TranslateAnimation translateAnimation;
    private final ScaleAnimation scaleAnimation;
    private final AlphaAnimation alphaAnimation;
    private final DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

    public MoveAnimation(final View v, float fromXDelta, float toXDelta, float fromYDelta,
        float toYDelta, final boolean upOrDown) {
      super(true);
      view = v;
      up = upOrDown;

      translateAnimation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
      translateAnimation.setInterpolator(decelerateInterpolator);
      translateAnimation.setAnimationListener(this);

      scaleAnimation =
          !up ? new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
              Animation.RELATIVE_TO_SELF, 1.0f)
              : new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                  Animation.RELATIVE_TO_SELF, 1.0f);
      alphaAnimation = !up ? new AlphaAnimation(1.0f, 0.0f) : new AlphaAnimation(0.0f, 1.0f);

      setDuration(500);
      setFillAfter(true);
      addAnimation(translateAnimation);
      addAnimation(scaleAnimation);
      addAnimation(alphaAnimation);
    }

    @Override public void onAnimationEnd(Animation animation) {
      mRunPredictiveAnimations = false;
      view.clearAnimation();
      if (!up) { //down
        removeCover();
      }
    }

    @Override public void onAnimationRepeat(Animation animation) {

    }

    @Override public void onAnimationStart(Animation animation) {
      mRunPredictiveAnimations = true;
    }
  }

  private final CasAdapterDataObserver mObserver = new CasAdapterDataObserver() {
    @Override public void onChanged() {
      refresh();
    }

    @Override public void onItemRangeChanged(int positionStart, int itemCount) {
    }

    @Override public void onItemRangeInserted(int positionStart, int itemCount) {
      if (mAttachedScrap.isEmpty()) {
        new RuntimeException("scrap heap must not be empty here");
      }
      addToLast(mAttachedScrap.get(0));
      mAttachedScrap.remove(0);
    }

    @Override public void onItemRangeRemoved(int positionStart, int itemCount) {
    }

    @Override public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
    }
  };

  public static abstract class CasAdapter<VH extends ViewHolder> {

    private final CasAdapterDataObservable mObservable = new CasAdapterDataObservable();

    public void registerAdapterDataObserver(CasAdapterDataObserver observer) {
      mObservable.registerObserver(observer);
    }

    public void unregisterAdapterDataObserver(CasAdapterDataObserver observer) {
      mObservable.unregisterObserver(observer);
    }

    public final void notifyDataSetChanged() {
      mObservable.notifyChanged();
    }

    public final void notifyItemRangeChanged(int positionStart, int itemCount) {
      mObservable.notifyItemRangeChanged(positionStart, itemCount);
    }

    public final void notifyItemRangeInserted(int positionStart, int itemCount) {
      mObservable.notifyItemRangeInserted(positionStart, itemCount);
    }

    public final void notifyItemMoved(int fromPosition, int toPosition) {
      mObservable.notifyItemMoved(fromPosition, toPosition);
    }

    public final void notifyItemRangeRemoved(int positionStart, int itemCount) {
      mObservable.notifyItemRangeRemoved(positionStart, itemCount);
    }

    public abstract VH onCreateView();

    public abstract void onBindViewHolder(VH holder, int position);

    public abstract int getItemCount();

    public final VH createView() {
      return onCreateView();
    }

    public final void bindViewHolder(VH holder, int position) {
      onBindViewHolder(holder, position);
    }
  }

  public abstract static class ViewHolder {
    public final View itemView;

    public ViewHolder(View v) {
      if (v == null) {
        throw new IllegalArgumentException("itemView must not be null");
      }
      itemView = v;
    }
  }
}
