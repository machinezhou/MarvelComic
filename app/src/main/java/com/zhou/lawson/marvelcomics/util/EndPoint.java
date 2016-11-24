package com.zhou.lawson.marvelcomics.util;

import com.zhou.lawson.marvelcomics.BuildConfig;

/**
 * Created by lawson on 16/11/1.
 */
public final class EndPoint {

  public static String baseUrl() {
    return BuildConfig.BASE_PATH;
  }

  public static String badUrl() {
    return "https://marvel";
  }

  public static String endPoint() {
    return BuildConfig.BASE_PATH + BuildConfig.API_VERSION;
  }

  public static String hash() {
    return BuildConfig.PARAM_HASH;
  }

  public static String key() {
    return BuildConfig.PARAM_KEY;
  }

  public static String timeStamp() {
    return BuildConfig.PARAM_TIMESTAMP;
  }

  public static String publicKeyValue() {
    return BuildConfig.PUBLIC_KEY;
  }

  public static String privateKeyValue() {
    return BuildConfig.PRIVATE_KEY;
  }

  private EndPoint() {
    // No instances.
  }
}
