package com.sky.entity;

import com.sky.vo.PassengerVO;
import lombok.Data;

@Data
   public class OrderPassengerRow {
    private Long orderId;
    private Long id;
    private String name;
    private String gender;
    private String idNo;
    private String phone;
    public PassengerVO toVO() {
        PassengerVO vo = new PassengerVO();
        vo.setId(id);
        vo.setName(name);
        vo.setGender(gender);
        vo.setIdNo(idNo);
        vo.setPhone(phone);
        return vo;
    }
}