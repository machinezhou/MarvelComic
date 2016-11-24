package com.zhou.lawson.marvelcomics.data.database;

import android.database.Cursor;

/**
 * Created by lawson on 16/7/23.
 */
public final class DatabaseUtil {
  public static final int BOOLEAN_FALSE = 0;
  public static final int BOOLEAN_TRUE = 1;

  public static String getCursorString(Cursor cursor, String columnName) {
    return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
  }

  public static boolean getCursorBoolean(Cursor cursor, String columnName) {
    return getCursorInt(cursor, columnName) == BOOLEAN_TRUE;
  }

  public static long getCursorLong(Cursor cursor, String columnName) {
    return cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
  }

  public static int getCursorInt(Cursor cursor, String columnName) {
    return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
  }

  private DatabaseUtil() {
    throw new AssertionError("No instances.");
  }
}
