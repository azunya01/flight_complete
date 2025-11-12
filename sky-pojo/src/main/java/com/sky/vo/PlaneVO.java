package com.sky.vo;

import com.sky.entity.SeatTypeCounts;
import lombok.Data;

import java.util.List;

@Data
public class PlaneVO {
   private Integer id;
   private String planeName;
   private List<SeatTypeCounts> seatTypeCounts;
}
