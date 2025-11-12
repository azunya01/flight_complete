// com.sky.vo.UserFlightVO
package com.sky.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserFlightVO {
    private Integer id;
    private String name;
    private String departureAirportName;
    private String arrivalAirportName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime departureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arrivalTime;
    private Double leastPrice;
    private Integer seatNumber;
}
