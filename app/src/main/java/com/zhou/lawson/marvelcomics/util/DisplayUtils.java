package com.zhou.lawson.marvelcomics.util;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public final class DisplayUtils {

  public static Point getDisplay(Context context) {
    final Point out = new Point();
    WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = manager.getDefaultDisplay();
    display.getSize(out);
    return out;
  }

  public static int getDisplayX(Context context) {
    return getDisplay(context).x;
  }

  public static int getDisplayY(Context context) {
    return getDisplay(context).y;
  }

  private DisplayUtils() {
    // No instances.
  }
}
