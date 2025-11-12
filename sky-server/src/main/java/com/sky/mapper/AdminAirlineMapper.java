package com.sky.mapper;

import com.sky.entity.Airline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminAirlineMapper {


    Airline getByFromTo(String fromAirport, String toAirport);

    void insert(Airline airline);

    void delectAirlineById(Integer airlineId);

    Airline getById(Integer airlineId);


    void update(Airline airline);
    // 全量分页
    long countAll();
    List<Airline> selectAllPaged(@Param("offset") int offset, @Param("size") int size);

    // 条件分页
    long countByFromTo(@Param("fromAirport") String fromAirport,
                       @Param("toAirport") String toAirport);

    List<Airline> selectByFromToPaged(@Param("fromAirport") String fromAirport,
                                      @Param("toAirport") String toAirport,
                                      @Param("offset") int offset,
                                      @Param("size") int size);
}
