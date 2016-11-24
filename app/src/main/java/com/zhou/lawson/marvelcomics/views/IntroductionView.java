package com.zhou.lawson.marvelcomics.views;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.zhou.lawson.marvelcomics.R;

/**
 * Created by lawson on 16/11/10.
 */

public class IntroductionView extends FrameLayout {

  private Unbinder unbinder;
  @BindView(R.id.introduction) SimpleDraweeView introductionView;
  private DraweeController controller;

  public IntroductionView(Context context) {
    super(context);
  }

  public IntroductionView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public IntroductionView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    inflate(getContext(), R.layout.layout_introduction, this);
    unbinder = ButterKnife.bind(this);
    init();
  }

  private void init() {
    controller = Fresco.newDraweeControllerBuilder()
        .setUri("res://com.zhou.lawson.marvelcomics/" + R.raw.introduction_bg_2)
        .setControllerListener(controllerListener)
        .build();
    introductionView.setController(controller);
    controller.onAttach();
  }

  private final BaseControllerListener<ImageInfo> controllerListener =
      new BaseControllerListener<ImageInfo>() {
        @Override public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo,
            @Nullable Animatable anim) {
          if (anim != null) {
            anim.start();
          }
        }
      };

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (unbinder != null) {
      unbinder.unbind();
    }
    controller.onDetach();
  }
}
