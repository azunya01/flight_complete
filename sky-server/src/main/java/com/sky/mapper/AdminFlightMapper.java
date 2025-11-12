package com.sky.mapper;

import com.sky.entity.Flight;
import com.sky.entity.FlightSeatInventory;
import com.sky.vo.FlightSeatVO;
import com.sky.vo.FlightVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AdminFlightMapper {


    Flight getFlightById(@Param("id") Integer id);

    int countByNameExcludingId(@Param("name") String name, @Param("id") Integer id);

    int updateFlight(Flight flight);

    int deleteInventoryByFlightId(@Param("flightId") Integer flightId);

    int batchInsertInventory(@Param("list") List<FlightSeatInventory> list);

    int updateInventoryOne(@Param("flightId") Integer flightId,
                           @Param("seatTypeId") Integer seatTypeId,
                           @Param("available") Integer available,
                           @Param("price") BigDecimal price);

    List<FlightSeatVO> listSeatViewByFlightId(@Param("flightId") Integer flightId);

    Flight getFlightByFlightName(String name);

    void insertFlight(Flight flight);

    void deleteFlightById(Integer id);

    long countAllFlights();

    List<FlightVO> pageFlightList(@Param("offset") int offset,
                                  @Param("size") int size);
    FlightVO getFlightViewById(@Param("id") Integer id);
}
