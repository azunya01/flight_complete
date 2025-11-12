// com.sky.dto.PassengerCreateDTO
package com.sky.dto;
import lombok.Data;

@Data
public class PassengerCreateDTO {
    private Integer id;
    private Integer userId;   // 谁的常用乘客
    private String name;
    private String gender;
    private String idNo;
    private String phone;
}