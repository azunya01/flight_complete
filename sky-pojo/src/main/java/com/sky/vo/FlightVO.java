package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FlightVO {
   private Integer id;
   private String name;
   private String planeName;
   private String departureAirportName;
    private     String arrivalAirportName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
 private    LocalDateTime departureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
 private    LocalDateTime arrivalTime;
   private List<FlightSeatVO> flightSeatVOS;
}
