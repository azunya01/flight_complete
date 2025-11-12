package com.sky.entity;

import lombok.Data;

@Data
public class PlaneSeatCapacity {
   private Integer planeId;
   private Integer seatTypeId;
   private Integer capacity;
   private Integer rowNumber;
}
