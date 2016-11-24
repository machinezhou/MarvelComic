package com.zhou.lawson.marvelcomics.views.loading;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import com.zhou.lawson.marvelcomics.R;

public final class LoadingDialog extends Dialog {

  private LoadingView loadingView;

  public LoadingDialog(Context context) {
    super(context, R.style.Marvel_Dialog_Transparent);
    setCanceledOnTouchOutside(false);
    setContentView(R.layout.layout_dialog_loading);
    loadingView = (LoadingView) findViewById(R.id.loading_view);
    Window window = getWindow();
    WindowManager.LayoutParams lp;
    if (window != null) {
      lp = window.getAttributes();
      lp.gravity = Gravity.CENTER;
      lp.dimAmount = 0.5f;
      lp.alpha = 1.0f;
      window.setAttributes(lp);
      window.setWindowAnimations(R.style.Marvel_Dialog_Animation_Fade);
      WindowManager.LayoutParams wl = window.getAttributes();
      window.setAttributes(wl);
    }
  }

  private void startLoading() {
    loadingView.start();
  }

  private void stopLoading() {
    loadingView.stop();
  }

  @Override public void show() {
    super.show();
    startLoading();
  }

  @Override public void dismiss() {
    super.dismiss();
    stopLoading();
  }
}
