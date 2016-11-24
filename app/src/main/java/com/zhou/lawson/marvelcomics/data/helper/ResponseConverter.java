package com.zhou.lawson.marvelcomics.data.helper;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import timber.log.Timber;

import static okhttp3.internal.Util.UTF_8;

/**
 * Created by lawson on 16/11/8.
 */

public class ResponseConverter<T> implements Converter<ResponseBody, T> {

  private final Gson gson;
  private final TypeAdapter<T> adapter;

  ResponseConverter(Gson gson, TypeAdapter<T> adapter) {
    this.gson = gson;
    this.adapter = adapter;
  }

  @Override public T convert(ResponseBody value) throws IOException {
    Timber.e("-->> Response convert");
    String response = value.string();
    /**
     * here we can do intercept converting from string to object
     */

    MediaType contentType = value.contentType();
    Charset charset = contentType != null ? contentType.charset(UTF_8) : UTF_8;
    InputStream inputStream = new ByteArrayInputStream(response.getBytes());
    Reader reader = new InputStreamReader(inputStream, charset);
    JsonReader jsonReader = gson.newJsonReader(reader);

    try {
      return adapter.read(jsonReader);
    } finally {
      value.close();
    }
  }
}
