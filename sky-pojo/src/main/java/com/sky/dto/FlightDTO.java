package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FlightDTO {
   private Integer id;
   private String name;
   private Integer planeId;
   private Integer airlineId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private LocalDateTime departureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private LocalDateTime arrivalTime;
   private List<FlightSeatPriceDTO> seatPrices;
}
