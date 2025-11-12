package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long orderId;
    private Integer flightId;
    private String flightName;
    private String from_airport;
    private String to_airport;
    private Integer seatTypeId;
    private String seatTypeName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime departureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arrivalTime;
    private BigDecimal price;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private List<PassengerVO> passengers;
}
