package com.sky.entity;

import lombok.Data;

@Data
public class PlaneSeat {
    private Integer planeId;
    private String  seatNo;
    private Integer seatTypeId;
    private Integer rowNo;
    private Integer colNo;
}
