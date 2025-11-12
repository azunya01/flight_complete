package com.sky.mapper;

import com.sky.entity.SeatType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminSeatTypeMapper {

    void insert(SeatType st);

    void update(SeatType st);

    void deleteById(@Param("id") Integer id);

    SeatType getById(@Param("id") Integer id);

    SeatType getByCode(@Param("code") String code);

    SeatType getByName(@Param("name") String name);

    List<SeatType> selectAll();
}
