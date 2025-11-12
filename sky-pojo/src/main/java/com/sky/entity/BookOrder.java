package com.sky.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookOrder {
    private Integer id;
    private Integer userId;
    private Integer flightId;
    private Integer status; // 0: booked, 1: cancelled, 2: completed
    private BigDecimal totalPrice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
