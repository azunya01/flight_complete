package com.sky.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
public class SeatOption {
    private Integer seatTypeId;
    private Integer available;
    private BigDecimal price;

}