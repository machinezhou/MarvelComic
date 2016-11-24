package com.zhou.lawson.marvelcomics.data.models;

import com.lawson.TableInfo;
import java.util.List;

/**
 * Created by lawson on 16/11/17.
 */

@TableInfo(ComicDetailModel.class) public class ComicDetailModel {

  public int id;
  public int digitalId;
  public String title;
  public String description;
  public String modified;
  public Thumbnail thumbnail;
  public List<Images> images;
}
