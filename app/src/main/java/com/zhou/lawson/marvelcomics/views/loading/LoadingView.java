package com.zhou.lawson.marvelcomics.views.loading;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.util.AnimationUtils;
import com.zhou.lawson.marvelcomics.views.animation.ColorTypeEvaluator;

/**
 * Created by lawson on 16/11/8.
 */

public class LoadingView extends View {

  public static final int DEFAULT_OFFSET = 10;
  public static final int DEFAULT_PADDING = 20;
  public static final float RATIO_WH = 0.624f;  // ratio of loading icon w/h

  private final Paint paint = new Paint();
  private final Paint glowPaint = new Paint();
  private ObjectAnimator objectAnimator;
  private ValueAnimator colorAnimator;
  private Rect rect = new Rect();
  private RectF glowRect = new RectF();
  private Bitmap bitmap;
  private int width;
  private int height;
  private int currentOffset = 0;
  private int glowColor = Color.TRANSPARENT;

  public LoadingView(Context context) {
    this(context, null);
  }

  public LoadingView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(DEFAULT_OFFSET);
    paint.setColor(ContextCompat.getColor(context, R.color.darkBlue));
    paint.setAntiAlias(true);
    glowPaint.setStyle(Paint.Style.FILL);
    bitmap = decodeResource(context.getResources(), R.drawable.loading_icon_3);

    objectAnimator = ObjectAnimator.ofFloat(this, "currentOffset", 0, DEFAULT_OFFSET);
    objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    objectAnimator.setDuration(600);
    objectAnimator.setRepeatCount(Animation.INFINITE);
    objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
    objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      public void onAnimationUpdate(ValueAnimator animation) {
        float animatedFraction = AnimationUtils.getAnimatedFraction(animation);
        float offset = animatedFraction * DEFAULT_OFFSET;
        setCurrentOffset(offset);
      }
    });
    //from black to white
    colorAnimator = ValueAnimator.ofObject(new ColorTypeEvaluator(), 0xff000000, 0xffffffff);
    colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    colorAnimator.setDuration(2000);
    colorAnimator.setRepeatCount(Animation.INFINITE);
    colorAnimator.setRepeatMode(ValueAnimator.REVERSE);
    colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        glowColor = (int) animation.getAnimatedValue();
      }
    });
  }

  private void setCurrentOffset(float offset) {
    currentOffset = (int) offset;
    invalidate();
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawCircle(width / 2, height / 2, width / 2 - DEFAULT_OFFSET, paint);
    canvas.drawCircle(width / 2, height / 2, width / 2 - DEFAULT_OFFSET - currentOffset, paint);

    rect.top = DEFAULT_PADDING + currentOffset;
    rect.bottom = height - DEFAULT_PADDING + currentOffset;

    glowRect.left = rect.left + DEFAULT_PADDING;
    glowRect.top = rect.centerY() - 50;
    glowRect.right = rect.right - DEFAULT_PADDING;
    glowRect.bottom = rect.centerY() + 50;

    glowPaint.setColor(glowColor);
    canvas.drawRect(glowRect, glowPaint);

    canvas.drawBitmap(bitmap, null, rect, paint);
  }

  public void start() {
    if (!objectAnimator.isRunning()) {
      objectAnimator.start();
      colorAnimator.start();
    }
  }

  public void stop() {
    if (objectAnimator.isRunning()) {
      objectAnimator.cancel();
      colorAnimator.cancel();
    }
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    width = w;
    height = h;

    final float bitmapWidth = h * RATIO_WH;

    rect.left = (int) (w - bitmapWidth) / 2;
    rect.top = DEFAULT_PADDING;
    rect.right = (int) (rect.left + bitmapWidth);
    rect.bottom = h - DEFAULT_PADDING;
  }

  private Bitmap decodeResource(Resources resources, int id) {
    TypedValue value = new TypedValue();
    resources.openRawResource(id, value);
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inTargetDensity = value.density;
    return BitmapFactory.decodeResource(resources, id, opts);
  }
}
