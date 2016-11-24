package com.zhou.lawson.marvelcomics.data;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import com.google.gson.Gson;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import com.zhou.lawson.marvelcomics.BuildConfig;
import com.zhou.lawson.marvelcomics.MarvelApplication;
import com.zhou.lawson.marvelcomics.data.database.DatabaseHelper;
import com.zhou.lawson.marvelcomics.data.helper.BaseConverterFactory;
import com.zhou.lawson.marvelcomics.data.helper.CacheInterceptor;
import com.zhou.lawson.marvelcomics.data.helper.RxErrorHandlingCallAdapterFactory;
import com.zhou.lawson.marvelcomics.data.models.BaseModel;
import com.zhou.lawson.marvelcomics.data.provider.AllProvider;
import com.zhou.lawson.marvelcomics.data.provider.AllService;
import com.zhou.lawson.marvelcomics.util.EndPoint;
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by lawson on 16/10/31.
 */
public final class Pool {

  private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

  private final MarvelApplication app;
  private final Gson gson;
  private final OkHttpClient client;
  private final Retrofit retrofit;
  private final BriteDatabase database;
  private final Picasso picasso;

  private SoftReference<HashMap<String, String>> tagsRef;
  private SoftReference<AllProvider> allProviderRef;

  public Pool(MarvelApplication application) {
    app = application;
    gson = new Gson();
    client = createOkHttpClient(application, getTagsRef());
    retrofit = createRetrofit(client, gson);
    database = createDatabase(application);
    picasso = createPicasso(application, client);
  }

  /**
   * return {@link Gson} instance
   */
  public Gson getGson() {
    return gson;
  }

  /**
   * return eTag map
   */
  public HashMap<String, String> getTagsRef() {
    if (tagsRef != null && tagsRef.get() != null) {
      return tagsRef.get();
    } else {
      tagsRef = new SoftReference<>(new HashMap<String, String>());
      return tagsRef.get();
    }
  }

  /**
   * return {@link OkHttpClient} instance
   */
  public OkHttpClient getOkHttpClient() {
    return client;
  }

  /**
   * return {@link BriteDatabase} instance
   */
  public BriteDatabase getDatabase() {
    return database;
  }

  /**
   * return {@link Picasso} instance
   */
  public Picasso getPicasso() {
    return picasso;
  }

  /**
   * @return return retrofit provider
   */
  public AllProvider getAllProvider() {
    if (allProviderRef != null && allProviderRef.get() != null) {
      return allProviderRef.get();
    } else {
      allProviderRef =
          new SoftReference<>(new AllProvider(app, retrofit, AllService.class, transformer));
      return allProviderRef.get();
    }
  }

  /**
   * return {@link Picasso} instance
   */
  private static Picasso createPicasso(Application app, OkHttpClient client) {
    Picasso picasso = new Picasso.Builder(app).downloader(new OkHttp3Downloader(client))
        .loggingEnabled(BuildConfig.DEBUG)
        .indicatorsEnabled(BuildConfig.DEBUG)
        .listener(new Picasso.Listener() {
          @Override public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
            if (BuildConfig.DEBUG) Timber.e(exception, "Failed to load image: %s", uri);
          }
        })
        .build();
    Picasso.setSingletonInstance(picasso);
    return picasso;
  }

  /**
   * return {@link Retrofit} instance
   */
  private static Retrofit createRetrofit(OkHttpClient client, Gson gson) {
    return new Retrofit.Builder().baseUrl(EndPoint.endPoint())
        .client(client)
        .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
        .addConverterFactory(BaseConverterFactory.create(gson))
        .build();
  }

  /**
   * return {@link OkHttpClient} instance
   */
  private static OkHttpClient createOkHttpClient(MarvelApplication app,
      HashMap<String, String> tagsRef) {
    HttpLoggingInterceptor interceptor =
        new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
          @Override public void log(String message) {
            if (BuildConfig.DEBUG) Timber.tag("OkHttp").d(message);
          }
        });
    interceptor.setLevel(
        BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
    return new OkHttpClient.Builder().addInterceptor(interceptor)
        .addNetworkInterceptor(new CacheInterceptor(app, tagsRef))
        .cache(new Cache(new File(app.getCacheDir(), "HttpResponseCache"), MAX_DISK_CACHE_SIZE))
        .retryOnConnectionFailure(true)
        .readTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .build();
  }

  /**
   * return {@link BriteDatabase} instance
   */
  private static BriteDatabase createDatabase(Context context) {
    BriteDatabase db = SqlBrite.create(new SqlBrite.Logger() {
      @Override public void log(String message) {
        if (BuildConfig.DEBUG) Timber.tag("Database").v(message);
      }
    }).wrapDatabaseHelper(new DatabaseHelper(context), Schedulers.io());
    db.setLoggingEnabled(BuildConfig.DEBUG);
    return db;
  }

  /**
   * transformer for specific model which has high frequency of use
   */
  private final Observable.Transformer<BaseModel, BaseModel> transformer =
      new Observable.Transformer<BaseModel, BaseModel>() {
        @Override public Observable<BaseModel> call(Observable<BaseModel> observable) {
          return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
      };
}
