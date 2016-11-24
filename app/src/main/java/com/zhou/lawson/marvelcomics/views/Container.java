package com.zhou.lawson.marvelcomics.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.data.event.Trigger;
import com.zhou.lawson.marvelcomics.data.helper.rx.EndSubscriber;
import com.zhou.lawson.marvelcomics.util.AnimationUtils;
import com.zhou.lawson.marvelcomics.views.header.HeaderToggleDrawable;
import com.zhou.lawson.marvelcomics.views.header.HeaderToggleLayout;
import com.zhou.lawson.marvelcomics.views.slideindicator.SlideIndicator;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by lawson on 16/11/4.
 */

public final class Container extends FrameLayout implements Action1<Ship> {

  private final DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
  private final CompositeSubscription subscription = new CompositeSubscription();
  //private static final int WAIT_TO_TRANSFER_PERIOD = 9;
  private static final int WAIT_TO_TRANSFER_PERIOD = 3;

  private HeaderToggleLayout headerLayout;
  private SlideIndicator indicators;
  private IntroductionView introductionView;
  private ComicDetailLayout detailLayout;

  private ContentPager pager;
  private Context context;
  private ValueAnimator headerAnimator;
  private boolean show = false;
  private boolean introShowed = false;
  private int headerTop;

  public Container(Context context) {
    this(context, null);
  }

  public Container(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    Trigger.get().subscribeTrigger().subscribe(this);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (introShowed) {
      addMainViews();
    } else {
      addIntroView();
      waitToTransfer(WAIT_TO_TRANSFER_PERIOD);
    }
  }

  private void waitToTransfer(final int period) {
    subscription.add(Observable.interval(1, TimeUnit.SECONDS)
        .take(period)
        .subscribeOn(Schedulers.io())
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            introShowed = false;
          }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new EndSubscriber<Long>() {
          @Override public void onEnd() {
            introShowed = true;
          }

          @Override public void onError(Throwable throwable) {
            super.onError(throwable);
            Timber.e(throwable.getMessage());
          }

          /**
           * time goes like 0,1,2...
           * @param time
           */
          @Override public void onNext(Long time) {
            if ((time.intValue() + 1) == period) {
              removeView(introductionView);
              addMainViews();
            }
          }
        }));
  }

  private void addIntroView() {
    introductionView = new IntroductionView(context);
    introductionView.setLayoutParams(new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT));
    addView(introductionView);
  }

  void addMainViews() {
    final int height = context.getResources().getDimensionPixelOffset(R.dimen.header_size);
    headerLayout = new HeaderToggleLayout(context);
    headerLayout.setLayoutParams(new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height));
    indicators = new SlideIndicator(context);
    indicators.setLayoutParams(new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height));
    pager = new ContentPager(context);
    pager.setLayoutParams(new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT));

    addView(indicators);
    addView(pager);
    addView(headerLayout);
    indicators.setViewPager(pager);

    headerAnimator = ValueAnimator.ofFloat(0, height);
    headerAnimator.setInterpolator(decelerateInterpolator);
    headerAnimator.setDuration(200);
    headerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        float animatedFraction = AnimationUtils.getAnimatedFraction(animation);
        float distance = (show ? animatedFraction : (1 - animatedFraction)) * height;
        headerTop = (int) distance;
        requestLayout();
      }
    });
    headerTop = 0;
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int headerHeight = 0;
    if (headerLayout != null && ViewCompat.isAttachedToWindow(headerLayout)) {
      headerHeight = headerLayout.getLayoutParams().height;
      headerLayout.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(headerHeight, MeasureSpec.EXACTLY));
    }
    if (indicators != null && ViewCompat.isAttachedToWindow(indicators)) {
      indicators.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(headerHeight, MeasureSpec.EXACTLY));
    }
    if (pager != null && ViewCompat.isAttachedToWindow(pager)) {
      pager.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(getMeasuredHeight() - headerHeight, MeasureSpec.EXACTLY));
    }
    if (introductionView != null && ViewCompat.isAttachedToWindow(introductionView)) {
      introductionView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
    }
    if (detailLayout != null && ViewCompat.isAttachedToWindow(detailLayout)) {
      detailLayout.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
          MeasureSpec.makeMeasureSpec(getMeasuredHeight() - headerHeight, MeasureSpec.EXACTLY));
    }
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int headerHeight = 0;
    if (headerLayout != null && ViewCompat.isAttachedToWindow(headerLayout)) {
      headerHeight = headerLayout.getMeasuredHeight();
      headerLayout.layout(l, headerTop, r, headerTop + headerHeight);
    }
    if (indicators != null && ViewCompat.isAttachedToWindow(indicators)) {
      indicators.layout(l, t, r, headerHeight);
    }
    if (pager != null && ViewCompat.isAttachedToWindow(pager)) {
      pager.layout(l, headerHeight, r, headerHeight + pager.getMeasuredHeight());
    }
    if (introductionView != null && ViewCompat.isAttachedToWindow(introductionView)) {
      introductionView.layout(l, t, r, b);
    }
    if (detailLayout != null && ViewCompat.isAttachedToWindow(detailLayout)) {
      detailLayout.layout(l, headerHeight, r, headerHeight + detailLayout.getMeasuredHeight());
    }
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    if (show && introShowed && ev.getY() > 2 * headerLayout.getMeasuredHeight()) {
      toggleHeader();
      return true;
    }
    return super.dispatchTouchEvent(ev);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    cleanup();
  }

  private void cleanup() {
    removeAllViews();
    subscription.unsubscribe();
  }

  private void toggleHeader() {
    int orientation = headerLayout.getHeaderOrientation();
    this.show = (orientation != HeaderToggleDrawable.DOT_ORIENTATION_UP);
    headerLayout.setHeaderOrientation(show ? HeaderToggleDrawable.DOT_ORIENTATION_UP
        : HeaderToggleDrawable.DOT_ORIENTATION_UP_RIGHT);
    headerAnimator.start();
  }

  private void switchToContentPager() {
    removeView(detailLayout);
    addView(pager);
    headerLayout.setHeaderOrientation(HeaderToggleDrawable.DOT_ORIENTATION_LEFT_RIGHT);
  }

  private void switchToComicDetail(Ship ship) {
    removeView(pager);
    detailLayout = new ComicDetailLayout(context, ship);
    addView(detailLayout);
    headerLayout.setHeaderOrientation(HeaderToggleDrawable.DOT_ORIENTATION_LEFT);
  }

  private void switchToCharacterDetail(Ship ship) {
    Toast.makeText(context, "call", Toast.LENGTH_SHORT).show();
  }

  @Override public void call(Ship ship) {
    if (ComicsLayout.TAG.equals(ship.getTag())) {
      switchToComicDetail(ship);
    }
    if (ComicDetailLayout.TAG.equals(ship.getTag())) {
      switchToContentPager();
    }
    if (CharactersLayout.TAG.equals(ship.getTag())) {
      switchToCharacterDetail(ship);
    }
    if (HeaderToggleLayout.TAG.equals(ship.getTag())) {
      toggleHeader();
    }
  }
}
