package com.sky.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FlightSeatInventoryDTO {
  private   Integer seatTypeId;
  private   Integer available;
  private   BigDecimal price;
}
