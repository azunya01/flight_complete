package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sky.entity.SeatType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserFlightDetailVO {
    private Integer id;
    private String name;
    private String departureAirportName;
    private String arrivalAirportName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime departureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arrivalTime;
    private Double leastPrice;
    private List<UserSeatTypeVO> seatTypes;
}
