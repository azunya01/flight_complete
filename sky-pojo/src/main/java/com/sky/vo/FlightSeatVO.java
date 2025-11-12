package com.sky.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FlightSeatVO {
  private   String seatTypeName;
  private   BigDecimal price;
  private   Integer available;
  private   Integer total;
}
