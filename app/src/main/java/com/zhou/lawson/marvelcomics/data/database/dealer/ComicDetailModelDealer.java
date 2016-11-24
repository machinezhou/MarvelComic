package com.zhou.lawson.marvelcomics.data.database.dealer;

import android.content.ContentValues;
import android.database.Cursor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.sqlbrite.BriteDatabase;
import com.zhou.lawson.marvelcomics.data.models.ComicDetailListModel;
import com.zhou.lawson.marvelcomics.data.models.ComicDetailModel;
import com.zhou.lawson.marvelcomics.data.models.ComicDetailModel_TABLE;
import com.zhou.lawson.marvelcomics.data.models.Images;
import com.zhou.lawson.marvelcomics.data.models.Thumbnail;
import java.util.ArrayList;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.zhou.lawson.marvelcomics.data.database.DatabaseUtil.getCursorString;

/**
 * Created by lawson on 16/11/20.
 */

public class ComicDetailModelDealer extends SqliteDealer<ComicDetailModel> {
  private Gson gson;

  public ComicDetailModelDealer(Gson gson) {
    if (gson == null) {
      throw new NullPointerException("gson must be not null");
    }
    this.gson = gson;
  }

  @Override public ComicDetailModel fromCursor(Cursor cursor) {
    ComicDetailModel model = new ComicDetailModel();
    model.id = Integer.parseInt(getCursorString(cursor, ComicDetailModel_TABLE.ID_COL));
    model.digitalId =
        Integer.parseInt(getCursorString(cursor, ComicDetailModel_TABLE.DIGITALID_COL));
    model.title = getCursorString(cursor, ComicDetailModel_TABLE.TITLE_COL);
    model.description = getCursorString(cursor, ComicDetailModel_TABLE.DESCRIPTION_COL);
    model.modified = getCursorString(cursor, ComicDetailModel_TABLE.MODIFIED_COL);
    model.images =
        gson.<ArrayList<Images>>fromJson(getCursorString(cursor, ComicDetailModel_TABLE.IMAGES_COL),
            new TypeToken<ArrayList<Images>>() {
            }.getType());
    model.thumbnail = gson.fromJson(getCursorString(cursor, ComicDetailModel_TABLE.THUMBNAIL_COL),
        new TypeToken<Thumbnail>() {
        }.getType());
    return model;
  }

  @Override public ContentValues toContentValues(ComicDetailModel model) {
    ContentValues values = new ContentValues();
    values.put(ComicDetailModel_TABLE.ID_COL, model.id);
    values.put(ComicDetailModel_TABLE.DIGITALID_COL, model.digitalId);
    values.put(ComicDetailModel_TABLE.IMAGES_COL, gson.toJson(model.images));
    values.put(ComicDetailModel_TABLE.MODIFIED_COL, model.modified);
    values.put(ComicDetailModel_TABLE.THUMBNAIL_COL, gson.toJson(model.thumbnail));
    values.put(ComicDetailModel_TABLE.TITLE_COL, model.title);
    values.put(ComicDetailModel_TABLE.DESCRIPTION_COL, gson.toJson(model.description));
    return values;
  }

  /**
   * save to database
   *
   * @param refresh true from refresh, false from load more
   */
  public Observable.Transformer<ComicDetailListModel, ComicDetailListModel> saveToDatabase(
      final boolean refresh, final BriteDatabase database) {
    return new Observable.Transformer<ComicDetailListModel, ComicDetailListModel>() {
      @Override
      public Observable<ComicDetailListModel> call(Observable<ComicDetailListModel> observable) {
        return observable.observeOn(Schedulers.newThread())
            .doOnNext(new Action1<ComicDetailListModel>() {
              @Override public void call(ComicDetailListModel o) {
                if (o != null && o.isSucceed() && o.data != null && o.data.results != null) {
                  BriteDatabase.Transaction transaction = database.newTransaction();
                  try {
                    if (refresh) {
                      database.execute(ComicDetailModel_TABLE.TABLE_DELETE);
                    }
                    for (ComicDetailModel model : o.data.results) {
                      database.insert(ComicDetailModel_TABLE.TABLE_NAME, toContentValues(model));
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
