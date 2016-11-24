package com.zhou.lawson.marvelcomics.data.provider;

import android.app.Application;
import com.zhou.lawson.marvelcomics.data.models.BaseModel;
import retrofit2.Retrofit;
import rx.Observable;

/**
 * Created by lawson on 16/11/1.
 */

public abstract class Provider<T> {
  protected final Application app;
  protected final T api;
  protected final Observable.Transformer<BaseModel, BaseModel> baseTransformer;

  public Provider(Application app, Retrofit retrofit, Class<T> cls,
      Observable.Transformer<BaseModel, BaseModel> t) {
    this.app = app;
    this.api = retrofit.create(cls);
    this.baseTransformer = t;
  }
}
