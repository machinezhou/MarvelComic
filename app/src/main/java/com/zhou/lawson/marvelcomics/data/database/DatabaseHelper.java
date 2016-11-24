package com.zhou.lawson.marvelcomics.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.zhou.lawson.marvelcomics.data.models.CharacterModel_TABLE;
import com.zhou.lawson.marvelcomics.data.models.ComicDetailModel_TABLE;
import com.zhou.lawson.marvelcomics.data.models.ComicModel_TABLE;

/**
 * Created by lawson on 16/11/1.
 */

public final class DatabaseHelper extends SQLiteOpenHelper {

  public static final String DATABASE_NAME = "marvel.db";
  /**
   * warning : every release should check version, if <bold>database<bold/> has change, version
   * code should change
   */
  public static final int DATABASE_VERSION = 3;

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL(ComicModel_TABLE.TABLE_CREATE);
    db.execSQL(CharacterModel_TABLE.TABLE_CREATE);
    db.execSQL(ComicDetailModel_TABLE.TABLE_CREATE);
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion != DATABASE_VERSION) {
      db.execSQL(ComicModel_TABLE.TABLE_DROP);
      db.execSQL(CharacterModel_TABLE.TABLE_DROP);
      db.execSQL(ComicDetailModel_TABLE.TABLE_DROP);
      onCreate(db);
    }
  }
}
