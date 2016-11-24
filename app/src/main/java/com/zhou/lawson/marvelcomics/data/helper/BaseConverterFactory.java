package com.zhou.lawson.marvelcomics.data.helper;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by lawson on 16/11/8.
 */

public class BaseConverterFactory extends Converter.Factory {
  private final Gson gson;

  private BaseConverterFactory(Gson gson) {
    if (gson == null) throw new NullPointerException("gson == null");
    this.gson = gson;
  }

  public static BaseConverterFactory create() {
    return create(new Gson());
  }

  public static BaseConverterFactory create(Gson gson) {
    return new BaseConverterFactory(gson);
  }

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
      Retrofit retrofit) {
    TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
    return new ResponseConverter<>(gson, adapter);
  }

  @Override public Converter<?, RequestBody> requestBodyConverter(Type type,
      Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
    TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
    return new RequestConverter<>(gson, adapter);
  }
}
