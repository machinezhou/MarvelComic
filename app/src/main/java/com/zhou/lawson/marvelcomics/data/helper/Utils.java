package com.zhou.lawson.marvelcomics.data.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import timber.log.Timber;

/**
 * Created by lawson on 16/11/1.
 */
class Utils {

  /**
   * generate hash from public/private key
   */
  static String hash(String publicKey, String privateKey) {
    String hashStr = "";
    try {
      String timeStamp = getUnixTimeStamp();
      String marvelData = timeStamp + privateKey + publicKey;
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      byte[] hash = messageDigest.digest(marvelData.getBytes());
      StringBuilder stringBuilder = new StringBuilder(2 * hash.length);
      for (byte b : hash)
        stringBuilder.append(String.format("%02x", b & 0xff));
      hashStr = stringBuilder.toString();
    } catch (NoSuchAlgorithmException e) {
      Timber.e(e.getMessage());
    }
    return hashStr;
  }

  static String getUnixTimeStamp() {
    return String.valueOf(System.currentTimeMillis() / 1000L);
  }
}
