package com.sky.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookDTO {
    private Integer userId;
    private Integer flightId;
    private Integer seatTypeId;
    private List<PassengerDTO> passengers;
}