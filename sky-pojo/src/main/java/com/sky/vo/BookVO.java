package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookVO {
    private String orderId;
    private String flightName;
    private BigDecimal totalPrice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    List<PassengerVO> passengers;
}
