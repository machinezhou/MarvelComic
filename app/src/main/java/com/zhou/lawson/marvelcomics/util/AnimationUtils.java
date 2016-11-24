package com.zhou.lawson.marvelcomics.util;

import android.animation.ValueAnimator;

/**
 * Created by lawson on 16/11/4.
 */

public class AnimationUtils {
  public static float getAnimatedFraction(ValueAnimator animator) {
    float fraction = animator.getDuration() > 0L ? (float) animator.getCurrentPlayTime()
        / (float) animator.getDuration() : 0.0F;
    fraction = Math.min(fraction, 1.0F);
    fraction = animator.getInterpolator().getInterpolation(fraction);
    return fraction;
  }
}
