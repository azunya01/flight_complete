package com.sky.entity;

import lombok.Data;
import lombok.Getter;

// 简单行映射
@Data
public class OrderBase {
    private Integer flightId;
    private Integer status;
}