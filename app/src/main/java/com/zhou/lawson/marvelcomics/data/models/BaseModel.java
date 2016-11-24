package com.zhou.lawson.marvelcomics.data.models;

/**
 * Created by lawson on 16/11/1.
 */

public class BaseModel {

  /**
   * example:
   *
   * "code": 200,
   * "status": "Ok",
   * "copyright": "© 2016 MARVEL",
   * "attributionText": "Data provided by Marvel. © 2016 MARVEL",
   * "attributionHTML": "<a href=\"http://marvel.com\">Data provided by Marvel. © 2016 MARVEL</a>",
   * "etag": "42d963028c8e2b410acdb13e21093903ae6ccbea",
   */
  public int code;
  public String status;
  public String copyright;
  public String attributionText;
  public String attributionHTML;
  public String etag;

  public boolean isValidCookie() {
    return true;
  }

  public boolean isSucceed() {
    return 200 == code && "Ok".equals(status);
  }
}
