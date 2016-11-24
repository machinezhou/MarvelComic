package com.zhou.lawson.marvelcomics.data.models;

import java.util.List;

/**
 * Created by lawson on 16/11/15.
 */

public class CharacterListModel extends BaseModel {

  public DataModel data;

  public static class DataModel extends ResultModel {
    public List<CharacterModel> results;
  }
}
