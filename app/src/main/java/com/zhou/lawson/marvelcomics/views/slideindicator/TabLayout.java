package com.zhou.lawson.marvelcomics.views.slideindicator;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.zhou.lawson.marvelcomics.R;

/**
 * Created by lawson on 16/11/7.
 */
public class TabLayout extends LinearLayout {

  public TabLayout(Context context) {
    this(context, null);
  }

  public TabLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
  }
}
