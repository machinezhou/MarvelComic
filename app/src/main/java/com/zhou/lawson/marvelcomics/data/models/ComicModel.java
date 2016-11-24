package com.zhou.lawson.marvelcomics.data.models;

import com.lawson.TableInfo;
import java.util.List;

/**
 * Created by lawson on 16/11/1.
 */
@TableInfo(ComicModel.class) public class ComicModel {

  /**
   * id : 42882
   * digitalId : 26110
   * title : Lorna the Jungle Girl (1954) #6
   * modified : 2015-10-15T11:13:52-0400
   * format : Comic
   * pageCount : 32
   * urls :
   * [
   * {"type":"detail","url":"http://marvel.com/comics/issue/42882/lorna_the_jungle_girl_1954_6?
   * utm_campaign=apiRef&utm_source=37425d749b0301ff752ffcb8f996acc5"},
   * {"type":"reader","url":"http://marvel.com/digitalcomics/view.htm?
   * iid=26110&utm_campaign=apiRef&utm_source=37425d749b0301ff752ffcb8f996acc5"}]
   * prices : [{"type":"printPrice","price":0}
   * ]
   * thumbnail : {"path":"http://i.annihil.us/u/prod/marvel/i/mg/9/40/50b4fc783d30f","extension":"jpg"}
   * images : [{"path":"http://i.annihil.us/u/prod/marvel/i/mg/9/40/50b4fc783d30f","extension":"jpg"}]
   */

  public int id;
  public int digitalId;
  public String title;
  public String modified;
  public String format;
  public int pageCount;
  public Thumbnail thumbnail;
  public List<Link> urls;
  public List<Images> images;
}
