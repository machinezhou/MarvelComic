package com.zhou.lawson.marvelcomics.util;

import java.util.List;

/**
 * Created by lawson on 16/5/5.
 */
public final class CheckUtils {

  public static boolean isEmpty(CharSequence str) {
    return str == null || str.length() == 0;
  }

  /**
   * one of them empty will return true
   *
   * @param str strings
   */
  public static boolean isEmpty(CharSequence... str) {
    if (str == null || str.length == 0) {
      return true;
    }
    for (CharSequence s : str) {
      if (s == null || s.length() == 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * size of list is 0, return true
   *
   * @param list list
   */
  public static boolean isEmpty(List list) {
    return list == null || list.size() == 0;
  }
}
