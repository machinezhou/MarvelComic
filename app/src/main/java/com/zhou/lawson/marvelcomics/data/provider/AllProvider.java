package com.zhou.lawson.marvelcomics.data.provider;

import android.app.Application;
import com.zhou.lawson.marvelcomics.data.models.BaseModel;
import com.zhou.lawson.marvelcomics.data.models.CharacterListModel;
import com.zhou.lawson.marvelcomics.data.models.ComicDetailListModel;
import com.zhou.lawson.marvelcomics.data.models.ComicListModel;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by lawson on 16/11/1.
 *
 * no different functional module, so get them all together, otherwise should divide them.
 */
public class AllProvider extends Provider<AllService> {

  public AllProvider(Application app, Retrofit retrofit, Class<AllService> cls,
      Observable.Transformer<BaseModel, BaseModel> t) {
    super(app, retrofit, cls, t);
  }

  public Observable<ComicListModel> getComics(int offset) {
    return api.getComics(offset)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  public Observable<ComicDetailListModel> getComicDetail(int id) {
    return api.getComicDetail(id)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  public Observable<CharacterListModel> getCharacters(int offset) {
    return api.getCharacters(offset)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }
}
