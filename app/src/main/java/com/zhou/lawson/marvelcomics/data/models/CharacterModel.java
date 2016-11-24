package com.zhou.lawson.marvelcomics.data.models;

import com.lawson.TableInfo;

/**
 * Created by lawson on 16/11/15.
 */
@TableInfo(CharacterModel.class) public class CharacterModel {

  /**
   * id : 1011334
   * name : 3-D Man
   * description :
   * modified : 2014-04-29T14:18:17-0400
   * thumbnail : {"path":"http://i.annihil.us/u/prod/marvel/i/mg/c/e0/535fecbbb9784","extension":"jpg"}
   */

  public int id;
  public String name;
  public String description;
  public String modified;
  public Thumbnail thumbnail;
}
