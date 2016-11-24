package com.zhou.lawson.marvelcomics.views.refresh;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.util.AnimationUtils;
import com.zhou.lawson.marvelcomics.views.animation.AnimatorListener;

/**
 * Created by lawson on 16/11/17.
 */

public class ArcView extends View {

  private final DecelerateInterpolator decInterpolator = new DecelerateInterpolator();
  private final AccelerateInterpolator accInterpolator = new AccelerateInterpolator();
  public static final int MAX_DOWN_SCROLL_DISTANCE = 600;

  private static final int DEFAULT_ARC_STROKE = 10;
  private final Path arcPath = new Path();
  private final Paint arcPaint = new Paint();
  private ObjectAnimator finishAnimator;
  private ObjectAnimator refreshAnim;
  private OnRefreshListener refreshListener;
  private Bitmap refreshCricle;
  private RectF refreshRect = new RectF();
  private boolean isRefresh = false;
  private float distance = 0;
  private float degree = 0;
  private float fraction = 1;
  private float arcHorizontalDistance;

  public ArcView(Context context) {
    this(context, null);
  }

  public ArcView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ArcView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    arcPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
    arcPaint.setStrokeWidth(DEFAULT_ARC_STROKE);
    arcPaint.setStyle(Paint.Style.FILL);
    arcPaint.setAntiAlias(true);
    arcPaint.setDither(true);
    arcPaint.setStrokeJoin(Paint.Join.ROUND);
    refreshCricle =
        BitmapFactory.decodeResource(getContext().getResources(), R.drawable.refresh_circle);
  }

  public void setRefreshListener(OnRefreshListener refreshListener) {
    this.refreshListener = refreshListener;
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    fraction = ((float) (h / 4)) / MAX_DOWN_SCROLL_DISTANCE;
    arcHorizontalDistance = ((float) (h / 2));
    distance = 0;

    setupAnimator();
  }

  private void setupAnimator() {
    final float maxDistance = getDecDistanceByScroll(MAX_DOWN_SCROLL_DISTANCE);
    finishAnimator = ObjectAnimator.ofFloat(this, "distance", maxDistance, 0f);
    finishAnimator.setInterpolator(accInterpolator);
    finishAnimator.setDuration(200);
    finishAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        float animatedFraction = AnimationUtils.getAnimatedFraction(animation);
        distance = (1 - animatedFraction) * maxDistance;
        invalidate();
      }
    });
    refreshAnim = ObjectAnimator.ofFloat(this, "degree", 0f, 360f);
    refreshAnim.setInterpolator(new LinearInterpolator());
    refreshAnim.setDuration(5000);
    refreshAnim.setRepeatCount(Animation.INFINITE);
    refreshAnim.setRepeatMode(ValueAnimator.REVERSE);
    refreshAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        float animatedFraction = AnimationUtils.getAnimatedFraction(animation);
        degree = animatedFraction * 360f;
        invalidate();
      }
    });
    refreshAnim.addListener(new AnimatorListener() {
      @Override public void onAnimationCancel(Animator animation) {
        finishAnimator.start();
      }
    });
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    drawArc(canvas);
    drawCircle(canvas);
  }

  private void drawArc(Canvas canvas) {
    final float arcPosition = getMeasuredHeight() * 3 / 4;
    arcPath.reset();
    arcPath.moveTo(getLeft(), arcPosition);
    arcPath.quadTo(getMeasuredWidth() * 4 / 5, arcPosition + distance, getRight(), arcPosition);
    arcPath.lineTo(getRight(), getMeasuredHeight());
    arcPath.lineTo(getLeft(), getMeasuredHeight());
    arcPath.close();
    canvas.drawPath(arcPath, arcPaint);
  }

  private void drawCircle(Canvas canvas) {
    final float arcPosition = getMeasuredHeight() * 3 / 4;
    refreshRect.left = arcPosition - distance * fraction;
    refreshRect.top = arcPosition - distance * fraction;
    refreshRect.bottom = arcPosition + distance * fraction;
    refreshRect.right = arcPosition + distance * fraction;
    if (refreshAnim.isRunning()) {
      canvas.save();
      canvas.rotate(degree, refreshRect.centerX(), refreshRect.centerY());
      canvas.drawBitmap(refreshCricle, null, refreshRect, arcPaint);
      canvas.restore();
    } else {
      canvas.drawBitmap(refreshCricle, null, refreshRect, arcPaint);
    }
  }

  private float getDecDistanceByScroll(float scroll) {
    return scroll * decInterpolator.getInterpolation(fraction);
  }

  private float getAccDistanceByScroll(float scroll) {
    return scroll * accInterpolator.getInterpolation(fraction);
  }

  /**
   * todo add scroll up gesture
   *
   * @param distanceY it'll be always negative
   * @return true consumed, false unconsumed
   */
  public boolean up(float distanceY) {
    if (Math.abs(distanceY) >= arcHorizontalDistance) {
      return false;
    } else {
      distance = distanceY;
      invalidate();
      return true;
    }
  }

  public void down(float distanceY) {
    if (finishAnimator.isRunning() || refreshAnim.isRunning()) return;
    if (distanceY >= MAX_DOWN_SCROLL_DISTANCE) {
      distanceY = MAX_DOWN_SCROLL_DISTANCE;
    }
    distance = getDecDistanceByScroll(distanceY);
    invalidate();
  }

  public void release(float distanceY) {
    if (finishAnimator.isRunning()) return;
    isRefresh = distanceY >= MAX_DOWN_SCROLL_DISTANCE;
    if (isRefresh) {
      refreshAnim.start();
      if (refreshListener != null) {
        refreshListener.refresh();
      }
    } else {
      final float currentDistance = getDecDistanceByScroll(distanceY);
      ValueAnimator unFinishAnimator = ValueAnimator.ofFloat(currentDistance, 0f);
      unFinishAnimator.setInterpolator(accInterpolator);
      unFinishAnimator.setDuration(200);
      unFinishAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override public void onAnimationUpdate(ValueAnimator animation) {
          float animatedFraction = AnimationUtils.getAnimatedFraction(animation);
          distance = (1 - animatedFraction) * currentDistance;
          invalidate();
        }
      });
      unFinishAnimator.start();
    }
  }

  public void setRefreshing(boolean r) {
    if (isRefresh != r) {
      isRefresh = r;
      if (isRefresh) {
        refreshAnim.start();
      } else {
        refreshAnim.cancel();
      }
    }
  }

  public interface OnRefreshListener {
    void refresh();
  }
}
