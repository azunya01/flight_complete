// com.sky.dto.PassengerUpdateDTO
package com.sky.dto;
import lombok.Data;

@Data
public class PassengerUpdateDTO {
    private Long id;         // 乘客ID
    private Integer userId;   // 所属用户（用于校验）
    private String name;
    private String gender;
    private String idNo;
    private String phone;
}