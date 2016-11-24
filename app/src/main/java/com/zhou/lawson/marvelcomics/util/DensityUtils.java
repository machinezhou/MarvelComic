package com.zhou.lawson.marvelcomics.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DensityUtils {

  public static int dip2px(Context context, float dpValue) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
        getDisplayMetrics(context));
  }

  public static int px2dip(Context context, float pxValue) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pxValue,
        getDisplayMetrics(context));
  }

  public static DisplayMetrics getDisplayMetrics(Context context) {
    return context.getResources().getDisplayMetrics();
  }

  public static int getDisplayDensity(Context context) {
    return (int) getDisplayMetrics(context).density;
  }

  private static float distanceBetween2Point(Context context, float x1, float y1, float x2,
      float y2) {
    float dx = x1 - x2;
    float dy = y1 - y2;
    float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
    return px2dip(context, distanceInPx);
  }
}
