package com.zhou.lawson.marvelcomics.views.header;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by lawson on 16/11/14.
 */
@IntDef({
    HeaderToggleDrawable.DOT_ORIENTATION_UP, HeaderToggleDrawable.DOT_ORIENTATION_UP_RIGHT,
    HeaderToggleDrawable.DOT_ORIENTATION_LEFT_RIGHT, HeaderToggleDrawable.DOT_ORIENTATION_LEFT
}) @Retention(RetentionPolicy.SOURCE) public @interface DotOrientation {
}
