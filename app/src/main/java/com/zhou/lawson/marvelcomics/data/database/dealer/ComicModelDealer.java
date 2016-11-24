package com.zhou.lawson.marvelcomics.data.database.dealer;

import android.content.ContentValues;
import android.database.Cursor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.sqlbrite.BriteDatabase;
import com.zhou.lawson.marvelcomics.data.models.ComicListModel;
import com.zhou.lawson.marvelcomics.data.models.ComicModel;
import com.zhou.lawson.marvelcomics.data.models.ComicModel_TABLE;
import com.zhou.lawson.marvelcomics.data.models.Images;
import com.zhou.lawson.marvelcomics.data.models.Link;
import com.zhou.lawson.marvelcomics.data.models.Thumbnail;
import java.util.ArrayList;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.zhou.lawson.marvelcomics.data.database.DatabaseUtil.getCursorString;

/**
 * Created by lawson on 16/11/10.
 */
public final class ComicModelDealer extends SqliteDealer<ComicModel> {
  private Gson gson;

  public ComicModelDealer(Gson gson) {
    if (gson == null) {
      throw new NullPointerException("gson must be not null");
    }
    this.gson = gson;
  }

  @Override public ComicModel fromCursor(Cursor cursor) {
    ComicModel model = new ComicModel();
    model.id = Integer.parseInt(getCursorString(cursor, ComicModel_TABLE.ID_COL));
    model.digitalId = Integer.parseInt(getCursorString(cursor, ComicModel_TABLE.DIGITALID_COL));
    model.format = getCursorString(cursor, ComicModel_TABLE.FORMAT_COL);
    model.images =
        gson.<ArrayList<Images>>fromJson(getCursorString(cursor, ComicModel_TABLE.IMAGES_COL),
            new TypeToken<ArrayList<Images>>() {
            }.getType());
    model.modified = getCursorString(cursor, ComicModel_TABLE.MODIFIED_COL);
    model.pageCount = Integer.parseInt(getCursorString(cursor, ComicModel_TABLE.PAGECOUNT_COL));
    model.thumbnail = gson.fromJson(getCursorString(cursor, ComicModel_TABLE.THUMBNAIL_COL),
        new TypeToken<Thumbnail>() {
        }.getType());
    model.title = getCursorString(cursor, ComicModel_TABLE.TITLE_COL);
    model.urls = gson.<ArrayList<Link>>fromJson(getCursorString(cursor, ComicModel_TABLE.URLS_COL),
        new TypeToken<ArrayList<Link>>() {
        }.getType());
    return model;
  }

  @Override public ContentValues toContentValues(ComicModel model) {
    ContentValues values = new ContentValues();
    values.put(ComicModel_TABLE.ID_COL, model.id);
    values.put(ComicModel_TABLE.DIGITALID_COL, model.digitalId);
    values.put(ComicModel_TABLE.FORMAT_COL, model.format);
    values.put(ComicModel_TABLE.IMAGES_COL, gson.toJson(model.images));
    values.put(ComicModel_TABLE.MODIFIED_COL, model.modified);
    values.put(ComicModel_TABLE.PAGECOUNT_COL, model.pageCount);
    values.put(ComicModel_TABLE.THUMBNAIL_COL, gson.toJson(model.thumbnail));
    values.put(ComicModel_TABLE.TITLE_COL, model.title);
    values.put(ComicModel_TABLE.URLS_COL, gson.toJson(model.urls));
    return values;
  }

  /**
   * save to database
   *
   * @param refresh true from refresh, false from load more
   */
  public Observable.Transformer<ComicListModel, ComicListModel> saveToDatabase(
      final boolean refresh, final BriteDatabase database) {
    return new Observable.Transformer<ComicListModel, ComicListModel>() {
      @Override public Observable<ComicListModel> call(Observable<ComicListModel> observable) {
        return observable.observeOn(Schedulers.newThread()).doOnNext(new Action1<ComicListModel>() {
          @Override public void call(ComicListModel o) {
            if (o != null && o.isSucceed() && o.data != null && o.data.results != null) {
              BriteDatabase.Transaction transaction = database.newTransaction();
              try {
                if (refresh) {
                  database.execute(ComicModel_TABLE.TABLE_DELETE);
                }
                for (ComicModel model : o.data.results) {
                  database.insert(ComicModel_TABLE.TABLE_NAME, toContentValues(model));
                }
                transaction.markSuccessful();
              } finally {
                transaction.end();
              }
            }
          }
        }).observeOn(AndroidSchedulers.mainThread());
      }
    };
  }
}
