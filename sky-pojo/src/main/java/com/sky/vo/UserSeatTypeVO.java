package com.sky.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserSeatTypeVO {
    private Integer seatTypeId;
    private String seatTypeName;
    private BigDecimal price;
    private boolean haveAvailable;
}
