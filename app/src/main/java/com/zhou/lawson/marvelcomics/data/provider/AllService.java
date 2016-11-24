package com.zhou.lawson.marvelcomics.data.provider;

import com.zhou.lawson.marvelcomics.data.models.CharacterListModel;
import com.zhou.lawson.marvelcomics.data.models.ComicDetailListModel;
import com.zhou.lawson.marvelcomics.data.models.ComicListModel;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by lawson on 16/11/1.
 *
 * no different functional module, so get them all together, otherwise should divide them.
 */
public interface AllService {
  @GET("comics") Observable<ComicListModel> getComics(@Query("offset") int offset);

  @GET("comics/{comicId}") Observable<ComicDetailListModel> getComicDetail(@Path("comicId") int comicId);

  @GET("characters") Observable<CharacterListModel> getCharacters(@Query("offset") int offset);
}
