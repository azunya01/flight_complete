package com.sky.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FlightSeatInventory {
 private  Integer flightId;
 private  Integer seatTypeId;
 private  Integer total;
 private  Integer available;
 private  BigDecimal price;
}
