package com.zhou.lawson.marvelcomics.views.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.data.models.ComicDetailModel;
import com.zhou.lawson.marvelcomics.views.single.TypefaceTextView;

/**
 * Created by lawson on 16/11/17.
 *
 * todo: should replace it with NestScroll due to untouchable scroll action in ScrollView.
 */
public class ScrollArcLayout extends ScrollView {

  @BindView(R.id.arc_view) ArcView arcView;
  @BindView(R.id.title) TypefaceTextView titleView;
  @BindView(R.id.time) TypefaceTextView timeView;
  @BindView(R.id.description) TypefaceTextView descriptionView;
  private float lastDownY;

  private Unbinder unbinder;

  public ScrollArcLayout(Context context) {
    super(context);
  }

  public ScrollArcLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ScrollArcLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (getChildCount() == 0) {
      inflate(getContext(), R.layout.layout_arc, this);
      unbinder = ButterKnife.bind(this);
      setVerticalScrollBarEnabled(false);
    }
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    float currentScrollY = getScrollY();
    float y = ev.getY();
    float distance = y - lastDownY;
    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
        lastDownY = y;
        break;
      case MotionEvent.ACTION_MOVE:
        if (distance >= 0 && currentScrollY == 0) {
          arcView.down(distance);
          return true;
        }
        break;
      case MotionEvent.ACTION_UP:
        if (distance >= 0 && currentScrollY == 0) {
          arcView.release(distance);
          return true;
        }
        break;
    }
    return super.dispatchTouchEvent(ev);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (unbinder != null) {
      unbinder.unbind();
    }
  }

  public void fillData(ComicDetailModel model, ArcView.OnRefreshListener listener) {
    if (model == null) {
      throw new NullPointerException("data is null");
    }
    titleView.setText(model.title);
    timeView.setText(model.modified);
    descriptionView.setText(model.description);
    arcView.setRefreshListener(listener);
  }

  public void setRefreshing(boolean r) {
    arcView.setRefreshing(r);
  }
}
