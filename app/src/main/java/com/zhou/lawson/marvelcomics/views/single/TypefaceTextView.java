package com.zhou.lawson.marvelcomics.views.single;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import com.zhou.lawson.marvelcomics.R;

import static com.zhou.lawson.marvelcomics.util.CheckUtils.isEmpty;

/**
 * Created by lawson on 16/11/1.
 *
 * TextView which is capable of loading ttf file
 */

public class TypefaceTextView extends TextView {

  public static final String DEFAULT_TYPE = "Roboto-Bold.ttf";

  public TypefaceTextView(Context context) {
    this(context, null);
  }

  public TypefaceTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public TypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    TypedArray a =
        context.getTheme().obtainStyledAttributes(attrs, R.styleable.TypefaceTextView, 0, 0);
    String typefaceName = a.getString(R.styleable.TypefaceTextView_typeface);
    if (isEmpty(typefaceName)) {
      typefaceName = DEFAULT_TYPE;
    }
    setTypeface(Typeface.createFromAsset(context.getAssets(), typefaceName));
    a.recycle();
  }
}
