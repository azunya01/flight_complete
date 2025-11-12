package com.sky.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Flight {
   private Integer id;
   private String name;
   private Integer planeId;
   private Integer airlineId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private LocalDateTime departureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private LocalDateTime arrivalTime;
}
