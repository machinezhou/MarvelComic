package com.zhou.lawson.marvelcomics.views.slideindicator;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.views.single.TypefaceTextView;

/**
 * Created by lawson on 16/11/7.
 */

public class Indicator extends FrameLayout {

  private int width;
  private int height;
  private TextView textView;

  public Indicator(Context context) {
    this(context, null);
  }

  public Indicator(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public Indicator(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setClickable(false);
  }

  public void setText(CharSequence text) {
    if (textView != null) {
      textView.setText(text);
    } else {
      textView = getDefaultIndicatorTextView(text);
      addView(textView);
    }
  }

  private TextView getDefaultIndicatorTextView(CharSequence text) {
    TypefaceTextView textView = new TypefaceTextView(getContext());
    textView.setText(text);
    textView.setTextSize(12);
    textView.getPaint().setFakeBoldText(true);
    textView.setTextColor(ContextCompat.getColor(getContext(), R.color.defaultTextColor));
    textView.setGravity(Gravity.CENTER);
    textView.setClickable(false);
    textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.tabColor));
    return textView;
  }

  public void setSize(int w, int h) {
    if (width <= 0 && height <= 0) {
      width = w;
      height = h;
      LayoutParams params = new LayoutParams(w, h);
      textView.setLayoutParams(params);
      textView.invalidate();
      setLayoutParams(params);
      invalidate();
    }
  }
}
