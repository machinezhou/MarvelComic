package com.zhou.lawson.marvelcomics.views;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.zhou.lawson.marvelcomics.MainActivity;
import com.zhou.lawson.marvelcomics.R;
import com.zhou.lawson.marvelcomics.data.event.BackTrigger;
import com.zhou.lawson.marvelcomics.data.event.Trigger;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by lawson on 16/11/13.
 */

public class ContentPager extends ViewPager {

  private static final String[] TITLES = {
      "COMICS", "CHARACTERS", "SERIES", "COLLECTIONS",
  };
  private final List<View> contents = new ArrayList<>();

  public ContentPager(Context context) {
    super(context);
  }

  public ContentPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    init();
  }

  private void init() {
    if (getChildCount() == 0) {
      BackTrigger.get().subscribeTrigger(backPressedAction);
      LayoutInflater inflater = LayoutInflater.from(getContext());
      View comicsView = inflater.inflate(R.layout.layout_comics_content, null);
      View charactersView = inflater.inflate(R.layout.layout_characters_content, null);
      View child2 = inflater.inflate(R.layout.layout_content, null);
      View child3 = inflater.inflate(R.layout.layout_content, null);
      contents.add(comicsView);
      contents.add(charactersView);
      contents.add(child2);
      contents.add(child3);
      setAdapter(pagerAdapter);
    }
  }

  private final PagerAdapter pagerAdapter = new PagerAdapter() {
    @Override public int getCount() {
      return TITLES.length;
    }

    @Override public CharSequence getPageTitle(int position) {
      return TITLES[position];
    }

    @Override public boolean isViewFromObject(View view, Object object) {
      return view == object;
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
      container.removeView(contents.get(position));
    }

    @Override public Object instantiateItem(ViewGroup container, int position) {
      container.addView(contents.get(position));
      return contents.get(position);
    }
  };

  private final Action1<Ship> backPressedAction = new Action1<Ship>() {
    @Override public void call(Ship ship) {
      int index = getCurrentItem();
      if (index != 0) {
        setCurrentItem(0);
      } else {
        Trigger.get().sendShipTrigger(Observable.just(new Ship(MainActivity.BACK_PRESSED)));
      }
      BackTrigger.get().subscribeTrigger(backPressedAction);
    }
  };
}
