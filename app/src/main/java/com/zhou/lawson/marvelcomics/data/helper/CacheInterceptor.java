package com.zhou.lawson.marvelcomics.data.helper;

import com.zhou.lawson.marvelcomics.MarvelApplication;
import com.zhou.lawson.marvelcomics.util.EndPoint;
import java.io.IOException;
import java.util.HashMap;
import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.zhou.lawson.marvelcomics.util.CheckUtils.isEmpty;
import static com.zhou.lawson.marvelcomics.util.NetworkUtils.isActiveNetwork;

/**
 * Created by lawson on 16/11/3.
 *
 * marvel api doesn't support normal etag mechanism because of its always changing url.
 * so I store etags manually, and they are only available before app finish.
 */

public final class CacheInterceptor implements Interceptor {

  public static final int MAX_AGE = 60 * 60;
  public static final int MAX_STALE = 60 * 60 * 24 * 28;

  private MarvelApplication application;
  private HashMap<String, String> eTagMap;

  public CacheInterceptor(MarvelApplication application, HashMap<String, String> eTagMap) {
    this.application = application;
    this.eTagMap = eTagMap;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    HttpUrl original = request.url();
    String tag = getETag(original);

    Request.Builder requestBuilder = request.newBuilder();

    if (!isActiveNetwork(application)) {
      //dead network force cache
      requestBuilder = requestBuilder.cacheControl(CacheControl.FORCE_CACHE).url(original);
    }
    request = requestBuilder.url(original.newBuilder()
        .addQueryParameter(EndPoint.key(), EndPoint.publicKeyValue())
        .addQueryParameter(EndPoint.hash(),
            Utils.hash(EndPoint.publicKeyValue(), EndPoint.privateKeyValue()))
        .addQueryParameter(EndPoint.timeStamp(), Utils.getUnixTimeStamp())
        .build()).addHeader("If-None-Match", tag).addHeader("ETag", tag).build();

    Response response = chain.proceed(request);

    if (isActiveNetwork(application)) {
      // read from cache for 1 minute
      response = response.newBuilder()
          .removeHeader("Pragma")
          .header("Cache-Control", "public, max-age=" + MAX_AGE)
          .build();
    } else {
      // tolerate 4-weeks stale
      response.newBuilder()
          .removeHeader("Pragma")
          .header("Cache-Control", "public, only-if-cached, max-stale=" + MAX_STALE)
          .build();
    }
    setETag(original, response.header("ETag"));
    return response;
  }

  /**
   * get etag from map by no-parameters-url
   *
   * @param url key in map
   * @return etag value
   */
  private String getETag(HttpUrl url) {
    String tag = eTagMap.get(url.url().toString());
    return isEmpty(tag) ? "" : tag;
  }

  /**
   * url-etag from server to override or add
   */
  private void setETag(HttpUrl url, String tag) {
    eTagMap.put(url.url().toString(), tag);
  }
}
