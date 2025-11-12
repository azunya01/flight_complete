package com.sky.vo;

import lombok.Data;

@Data
public class UserVO {
  private   Integer id;
  private   String name;
  private   String password;
  private   int type;
  private   String token;
}
