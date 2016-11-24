package com.zhou.lawson.marvelcomics.views.header;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.zhou.lawson.marvelcomics.util.AnimationUtils;
import com.zhou.lawson.marvelcomics.views.animation.AnimatorListener;

/**
 * Created by lawson on 16/11/4.
 */

public class HeaderToggleDrawable extends Drawable {

  public static final int DOT_ORIENTATION_UP = 0;
  public static final int DOT_ORIENTATION_UP_RIGHT = 1;
  public static final int DOT_ORIENTATION_LEFT_RIGHT = 2;
  public static final int DOT_ORIENTATION_LEFT = 3;

  private static final Interpolator DEFAULT_SWEEP_INTERPOLATOR = new DecelerateInterpolator();
  private static final float DEFAULT_DOT_RADIUS = 10;
  private static final float UP_RIGHT_SWEEP_ANGLE = 90;
  private static final float LEFT_RIGHT_SWEEP_ANGLE = 180;
  private Paint circlePaint = new Paint();
  private Paint dotPaint = new Paint();
  private RectF arc = new RectF();

  private ObjectAnimator upRightAppearingAnimator;
  private ObjectAnimator upRightDisappearingAnimator;
  private ObjectAnimator leftRightAppearingAnimator;
  private ObjectAnimator leftRightDisappearingAnimator;
  private float currentSweepAngle;
  private float rightDotRadius = DEFAULT_DOT_RADIUS;
  private float topDotRadius = 0;
  private float leftDotRadius = 0;
  private static final int SPACE = 10;
  private static final int CIRCLE_RADIUS = 30;

  /**
   * when toggle, indicate dot is moving(zooming) from right to top, top to right or switching
   * between left and right. default is right.
   */
  @DotOrientation private int orientation = DOT_ORIENTATION_UP_RIGHT;

  /**
   * when toggle, indicate arc is in step appearing or disappearing
   */
  private boolean appearing = false;

  public HeaderToggleDrawable(int color) {
    circlePaint.setAntiAlias(true);
    circlePaint.setStyle(Paint.Style.STROKE);
    circlePaint.setStrokeWidth(SPACE);
    circlePaint.setStrokeCap(Paint.Cap.ROUND);
    circlePaint.setColor(color);

    dotPaint.setAntiAlias(true);
    dotPaint.setStyle(Paint.Style.FILL);
    dotPaint.setColor(color);

    setupAnimations();
  }

  @Override public void draw(@NonNull Canvas canvas) {
    final Rect bounds = getBounds();
    int centerX = bounds.centerX();
    int centerY = bounds.centerY();

    canvas.drawCircle(centerX, centerY, CIRCLE_RADIUS, circlePaint);

    arc.left = centerX - CIRCLE_RADIUS - 2 * SPACE;
    arc.top = centerY - CIRCLE_RADIUS - 2 * SPACE;
    arc.right = centerX + CIRCLE_RADIUS + 2 * SPACE;
    arc.bottom = centerY + CIRCLE_RADIUS + 2 * SPACE;
    if (upRightAppearingAnimator.isRunning() || leftRightAppearingAnimator.isRunning()) {
      switch (orientation) {
        case DOT_ORIENTATION_UP:
          if (appearing) {
            canvas.drawArc(arc, 0, -currentSweepAngle, false, circlePaint);
          } else {
            canvas.drawArc(arc, -UP_RIGHT_SWEEP_ANGLE, currentSweepAngle, false, circlePaint);
          }
          break;
        case DOT_ORIENTATION_LEFT:
          if (appearing) {
            canvas.drawArc(arc, 0, currentSweepAngle, false, circlePaint);
          } else {
            canvas.drawArc(arc, LEFT_RIGHT_SWEEP_ANGLE, currentSweepAngle, false, circlePaint);
          }
          break;
        case DOT_ORIENTATION_UP_RIGHT:
          if (appearing) {
            canvas.drawArc(arc, -UP_RIGHT_SWEEP_ANGLE, currentSweepAngle, false, circlePaint);
          } else {
            canvas.drawArc(arc, 0, -currentSweepAngle, false, circlePaint);
          }
          break;
        case DOT_ORIENTATION_LEFT_RIGHT:
          if (appearing) {
            canvas.drawArc(arc, LEFT_RIGHT_SWEEP_ANGLE, currentSweepAngle, false, circlePaint);
          } else {
            canvas.drawArc(arc, currentSweepAngle, 0, false, circlePaint);
          }
          break;
        default:
          throw new IllegalArgumentException("illegal orientation" + orientation);
      }
    }

    canvas.drawCircle(centerX + CIRCLE_RADIUS + 2 * SPACE, centerY, rightDotRadius, dotPaint);
    canvas.drawCircle(centerX, centerY - CIRCLE_RADIUS - 2 * SPACE, topDotRadius, dotPaint);
    canvas.drawCircle(centerX - CIRCLE_RADIUS - 2 * SPACE, centerY, leftDotRadius, dotPaint);
  }

  public void toggle(@DotOrientation int orientation) {
    if (this.orientation == orientation) return;
    this.orientation = orientation;
    switch (orientation) {
      case DOT_ORIENTATION_UP:
      case DOT_ORIENTATION_UP_RIGHT:
        upRightAppearingAnimator.start();
        break;
      case DOT_ORIENTATION_LEFT:
      case DOT_ORIENTATION_LEFT_RIGHT:
        leftRightAppearingAnimator.start();
        break;
      default:
        throw new IllegalArgumentException("illegal orientation" + orientation);
    }
  }

  private void setupAnimations() {
    //step appearing
    upRightAppearingAnimator =
        ObjectAnimator.ofFloat(this, "currentSweepAngle", 0f, UP_RIGHT_SWEEP_ANGLE);
    upRightAppearingAnimator.setInterpolator(DEFAULT_SWEEP_INTERPOLATOR);
    upRightAppearingAnimator.setDuration(200);
    upRightAppearingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      public void onAnimationUpdate(ValueAnimator animation) {
        appearing = true;
        float animatedFraction = AnimationUtils.getAnimatedFraction(animation);
        float angle = animatedFraction * UP_RIGHT_SWEEP_ANGLE;
        float radius = animatedFraction * (DEFAULT_DOT_RADIUS / 2);
        setUpRightAppearingDotsRadius(radius);
        setCurrentSweepAngle(angle);
      }
    });
    upRightAppearingAnimator.addListener(new AnimatorListener() {
      boolean cancelled = false;

      public void onAnimationStart(Animator animation) {
        cancelled = false;
      }

      public void onAnimationEnd(Animator animation) {
        if (!cancelled) {
          upRightDisappearingAnimator.start();
        }
      }

      public void onAnimationCancel(Animator animation) {
        cancelled = true;
      }
    });
    leftRightAppearingAnimator =
        ObjectAnimator.ofFloat(this, "currentSweepAngle", 0f, LEFT_RIGHT_SWEEP_ANGLE);
    leftRightAppearingAnimator.setInterpolator(DEFAULT_SWEEP_INTERPOLATOR);
    leftRightAppearingAnimator.setDuration(200);
    leftRightAppearingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      public void onAnimationUpdate(ValueAnimator animation) {
        appearing = true;
        float animatedFraction = AnimationUtils.getAnimatedFraction(animation);
        float angle = animatedFraction * LEFT_RIGHT_SWEEP_ANGLE;
        float radius = animatedFraction * (DEFAULT_DOT_RADIUS / 2);
        setLeftRightAppearingDotsRadius(radius);
        setCurrentSweepAngle(angle);
      }
    });
    leftRightAppearingAnimator.addListener(new AnimatorListener() {
      boolean cancelled = false;

      public void onAnimationStart(Animator animation) {
        cancelled = false;
      }

      public void onAnimationEnd(Animator animation) {
        if (!cancelled) {
          leftRightDisappearingAnimator.start();
        }
      }

      public void onAnimationCancel(Animator animation) {
        cancelled = true;
      }
    });
    //step disappearing
    upRightDisappearingAnimator =
        ObjectAnimator.ofFloat(this, "currentSweepAngle", UP_RIGHT_SWEEP_ANGLE, 0);
    upRightDisappearingAnimator.setInterpolator(DEFAULT_SWEEP_INTERPOLATOR);
    upRightDisappearingAnimator.setDuration(200);
    upRightDisappearingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      public void onAnimationUpdate(ValueAnimator animation) {
        appearing = false;

        float animatedFraction = AnimationUtils.getAnimatedFraction(animation);
        float angle = (1 - animatedFraction) * UP_RIGHT_SWEEP_ANGLE;
        float radius = animatedFraction * (DEFAULT_DOT_RADIUS / 2);
        setUpRightDisappearingDotsRadius(radius);
        setCurrentSweepAngle(angle);
      }
    });
    leftRightDisappearingAnimator =
        ObjectAnimator.ofFloat(this, "currentSweepAngle", LEFT_RIGHT_SWEEP_ANGLE, 0);
    leftRightDisappearingAnimator.setInterpolator(DEFAULT_SWEEP_INTERPOLATOR);
    leftRightDisappearingAnimator.setDuration(200);
    leftRightDisappearingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      public void onAnimationUpdate(ValueAnimator animation) {
        appearing = false;

        float animatedFraction = AnimationUtils.getAnimatedFraction(animation);
        float angle = (1 - animatedFraction) * LEFT_RIGHT_SWEEP_ANGLE;
        float radius = animatedFraction * (DEFAULT_DOT_RADIUS / 2);
        setLeftRightDisappearingDotsRadius(radius);
        setCurrentSweepAngle(angle);
      }
    });
  }

  private void setUpRightAppearingDotsRadius(float radius) {
    if (orientation == DOT_ORIENTATION_UP) {
      rightDotRadius = DEFAULT_DOT_RADIUS - radius;
      topDotRadius = radius;
    } else {
      rightDotRadius = radius;
      topDotRadius = DEFAULT_DOT_RADIUS - radius;
    }
  }

  private void setLeftRightAppearingDotsRadius(float radius) {
    if (orientation == DOT_ORIENTATION_LEFT) {
      rightDotRadius = DEFAULT_DOT_RADIUS - radius;
      leftDotRadius = radius;
    } else {
      rightDotRadius = radius;
      leftDotRadius = DEFAULT_DOT_RADIUS - radius;
    }
  }

  private void setUpRightDisappearingDotsRadius(float radius) {
    float middle = DEFAULT_DOT_RADIUS / 2;
    if (orientation == DOT_ORIENTATION_UP) {
      rightDotRadius = middle - radius;
      topDotRadius = middle + radius;
    } else {
      rightDotRadius = middle + radius;
      topDotRadius = middle - radius;
    }
  }

  private void setLeftRightDisappearingDotsRadius(float radius) {
    float middle = DEFAULT_DOT_RADIUS / 2;
    if (orientation == DOT_ORIENTATION_LEFT) {
      rightDotRadius = middle - radius;
      leftDotRadius = middle + radius;
    } else {
      rightDotRadius = middle + radius;
      leftDotRadius = middle - radius;
    }
  }

  private void setCurrentSweepAngle(float angle) {
    currentSweepAngle = angle;
    invalidateSelf();
  }

  public int getOrientation() {
    return orientation;
  }

  @Override public void setAlpha(int alpha) {

  }

  @Override public void setColorFilter(ColorFilter colorFilter) {

  }

  @Override public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }
}
