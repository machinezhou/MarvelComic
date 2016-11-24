package com.zhou.lawson.marvelcomics.data.database.dealer;

import android.content.ContentValues;
import android.database.Cursor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.sqlbrite.BriteDatabase;
import com.zhou.lawson.marvelcomics.data.models.CharacterListModel;
import com.zhou.lawson.marvelcomics.data.models.CharacterModel;
import com.zhou.lawson.marvelcomics.data.models.CharacterModel_TABLE;
import com.zhou.lawson.marvelcomics.data.models.Thumbnail;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.zhou.lawson.marvelcomics.data.database.DatabaseUtil.getCursorString;

/**
 * Created by lawson on 16/11/15.
 */

public final class CharacterModelDealer extends SqliteDealer<CharacterModel> {

  private Gson gson;

  public CharacterModelDealer(Gson gson) {
    if (gson == null) {
      throw new NullPointerException("gson must be not null");
    }
    this.gson = gson;
  }

  @Override public CharacterModel fromCursor(Cursor cursor) {
    CharacterModel model = new CharacterModel();
    model.id = Integer.parseInt(getCursorString(cursor, CharacterModel_TABLE.ID_COL));
    model.modified = getCursorString(cursor, CharacterModel_TABLE.MODIFIED_COL);
    model.name = getCursorString(cursor, CharacterModel_TABLE.NAME_COL);
    model.description = getCursorString(cursor, CharacterModel_TABLE.DESCRIPTION_COL);
    model.thumbnail = gson.fromJson(getCursorString(cursor, CharacterModel_TABLE.THUMBNAIL_COL),
        new TypeToken<Thumbnail>() {
        }.getType());
    return model;
  }

  @Override public ContentValues toContentValues(CharacterModel model) {
    ContentValues values = new ContentValues();
    values.put(CharacterModel_TABLE.ID_COL, model.id);
    values.put(CharacterModel_TABLE.MODIFIED_COL, model.modified);
    values.put(CharacterModel_TABLE.NAME_COL, model.name);
    values.put(CharacterModel_TABLE.DESCRIPTION_COL, model.description);
    values.put(CharacterModel_TABLE.THUMBNAIL_COL, gson.toJson(model.thumbnail));
    return values;
  }

  /**
   * save to database
   *
   * @param refresh true from refresh, false from load more
   */
  public Observable.Transformer<CharacterListModel, CharacterListModel> saveToDatabase(
      final boolean refresh, final BriteDatabase database) {
    return new Observable.Transformer<CharacterListModel, CharacterListModel>() {
      @Override
      public Observable<CharacterListModel> call(Observable<CharacterListModel> observable) {
        return observable.observeOn(Schedulers.newThread())
            .doOnNext(new Action1<CharacterListModel>() {
              @Override public void call(CharacterListModel o) {
                if (o != null && o.isSucceed() && o.data != null && o.data.results != null) {
                  BriteDatabase.Transaction transaction = database.newTransaction();
                  try {
                    if (refresh) {
                      database.execute(CharacterModel_TABLE.TABLE_DELETE);
                    }
                    for (CharacterModel model : o.data.results) {
                      database.insert(CharacterModel_TABLE.TABLE_NAME, toContentValues(model));
                    }
                    transaction.markSuccessful();
                  } finally {
                    transaction.end();
                  }
                }
              }
            })
            .observeOn(AndroidSchedulers.mainThread());
      }
    };
  }
}
