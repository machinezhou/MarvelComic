package com.zhou.lawson.marvelcomics.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

  /**
   * return network info
   *
   * @See NetworkInfo
   */
  public static NetworkInfo getNetworkInfo(Context context) {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo();
  }

  /**
   * is current network connected or connecting
   */
  public static boolean isActiveNetwork(Context context) {
    NetworkInfo info = NetworkUtils.getNetworkInfo(context);
    return info != null && info.isConnectedOrConnecting();
  }

  private NetworkUtils() {
  }
}
