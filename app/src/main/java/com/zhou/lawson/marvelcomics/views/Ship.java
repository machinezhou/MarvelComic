package com.zhou.lawson.marvelcomics.views;

import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import java.util.ArrayList;

/**
 * Created by lawson on 16/11/13.
 */

public class Ship {
  private static final String TAG = "Ship";
  private SimpleArrayMap<String, Object> mMap = null;
  private String tag;

  public Ship(int capacity, String tag) {
    mMap = capacity > 0 ? new SimpleArrayMap<String, Object>(capacity)
        : new SimpleArrayMap<String, Object>();
    this.tag = tag;
  }

  public Ship() {
    this(0, TAG);
  }

  public Ship(String tag) {
    this(0, tag);
  }

  public int size() {
    return mMap.size();
  }

  public boolean isEmpty() {
    return mMap.isEmpty();
  }

  public void clear() {
    mMap.clear();
  }

  public String getTag() {
    return tag;
  }

  public void putBoolean(@Nullable String key, boolean value) {
    mMap.put(key, value);
  }

  public void putChar(@Nullable String key, char value) {
    mMap.put(key, value);
  }

  public void putInt(@Nullable String key, int value) {
    mMap.put(key, value);
  }

  public void putLong(@Nullable String key, long value) {
    mMap.put(key, value);
  }

  public void putFloat(@Nullable String key, float value) {
    mMap.put(key, value);
  }

  public void putDouble(@Nullable String key, double value) {
    mMap.put(key, value);
  }

  public void putString(@Nullable String key, @Nullable String value) {
    mMap.put(key, value);
  }

  public void putIntegerArrayList(@Nullable String key, @Nullable ArrayList<Integer> value) {
    mMap.put(key, value);
  }

  public void putStringArrayList(@Nullable String key, @Nullable ArrayList<String> value) {
    mMap.put(key, value);
  }

  public void putCharSequenceArrayList(@Nullable String key,
      @Nullable ArrayList<CharSequence> value) {
    mMap.put(key, value);
  }

  public boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    Object o = mMap.get(key);
    if (o == null) {
      return defaultValue;
    }
    try {
      return (Boolean) o;
    } catch (ClassCastException e) {
      typeWarning(key, o, "Boolean", defaultValue, e);
      return defaultValue;
    }
  }

  public char getChar(String key, char defaultValue) {
    Object o = mMap.get(key);
    if (o == null) {
      return defaultValue;
    }
    try {
      return (Character) o;
    } catch (ClassCastException e) {
      typeWarning(key, o, "Character", defaultValue, e);
      return defaultValue;
    }
  }

  public int getInt(String key) {
    return getInt(key, 0);
  }

  public int getInt(String key, int defaultValue) {
    Object o = mMap.get(key);
    if (o == null) {
      return defaultValue;
    }
    try {
      return (Integer) o;
    } catch (ClassCastException e) {
      typeWarning(key, o, "Integer", defaultValue, e);
      return defaultValue;
    }
  }

  public long getLong(String key) {
    return getLong(key, 0L);
  }

  public long getLong(String key, long defaultValue) {
    Object o = mMap.get(key);
    if (o == null) {
      return defaultValue;
    }
    try {
      return (Long) o;
    } catch (ClassCastException e) {
      typeWarning(key, o, "Long", defaultValue, e);
      return defaultValue;
    }
  }

  float getFloat(String key) {
    return getFloat(key, 0.0f);
  }

  float getFloat(String key, float defaultValue) {
    Object o = mMap.get(key);
    if (o == null) {
      return defaultValue;
    }
    try {
      return (Float) o;
    } catch (ClassCastException e) {
      typeWarning(key, o, "Float", defaultValue, e);
      return defaultValue;
    }
  }

  public double getDouble(String key) {
    return getDouble(key, 0.0);
  }

  public double getDouble(String key, double defaultValue) {
    Object o = mMap.get(key);
    if (o == null) {
      return defaultValue;
    }
    try {
      return (Double) o;
    } catch (ClassCastException e) {
      typeWarning(key, o, "Double", defaultValue, e);
      return defaultValue;
    }
  }

  @Nullable public String getString(@Nullable String key) {
    final Object o = mMap.get(key);
    try {
      return (String) o;
    } catch (ClassCastException e) {
      typeWarning(key, o, "String", e);
      return null;
    }
  }

  public String getString(@Nullable String key, String defaultValue) {
    final String s = getString(key);
    return (s == null) ? defaultValue : s;
  }

  void putObject(@Nullable String key, @Nullable Object value) {
    mMap.put(key, value);
  }

  Object getObject(@Nullable String key) {
    return mMap.get(key);
  }

  void typeWarning(String key, Object value, String className, Object defaultValue,
      ClassCastException e) {
    StringBuilder sb = new StringBuilder();
    sb.append("Key ");
    sb.append(key);
    sb.append(" expected ");
    sb.append(className);
    sb.append(" but value was a ");
    sb.append(value.getClass().getName());
    sb.append(".  The default value ");
    sb.append(defaultValue);
    sb.append(" was returned.");
    Log.w(TAG, sb.toString());
    Log.w(TAG, "Attempt to cast generated internal exception:", e);
  }

  void typeWarning(String key, Object value, String className, ClassCastException e) {
    typeWarning(key, value, className, "<null>", e);
  }
}
