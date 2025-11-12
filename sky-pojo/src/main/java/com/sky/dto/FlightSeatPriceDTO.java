package com.sky.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FlightSeatPriceDTO {
  private   Integer seatTypeId;
  private   BigDecimal price;
}
