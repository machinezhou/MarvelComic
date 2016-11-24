package com.zhou.lawson.marvelcomics.data.database.dealer;

import android.content.ContentValues;
import android.database.Cursor;
import com.squareup.sqlbrite.SqlBrite;
import java.util.ArrayList;
import java.util.List;
import rx.functions.Func1;

/**
 * Created by lawson on 16/7/25.
 *
 * dealer which deal with detail about something like crud operation between customer and provider
 */
public abstract class SqliteDealer<T> {

  /**
   * function of dealing with cursor
   */
  public Func1<SqlBrite.Query, List<T>> MAP = new Func1<SqlBrite.Query, List<T>>() {
    @Override public List<T> call(SqlBrite.Query query) {
      Cursor cursor = query.run();
      try {
        List<T> values = null;
        if (cursor != null) {
          values = new ArrayList<>(cursor.getCount());
          while (cursor.moveToNext()) {
            values.add(fromCursor(cursor));
          }
        }
        return values;
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
    }
  };

  /**
   * get data from cursor {@link Cursor}
   */
  public abstract T fromCursor(Cursor cursor);

  /**
   * save data to {@link ContentValues}
   */
  public abstract ContentValues toContentValues(T model);
}
