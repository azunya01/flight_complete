package com.sky.dto;

import com.sky.entity.SeatTypeCounts;
import lombok.Data;

import java.util.List;

@Data
public class PlaneDTO {
  private  String planeName;
  private  List<SeatTypeCounts> seatTypeCounts;
}
