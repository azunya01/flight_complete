package com.sky.mapper;


import com.sky.entity.Airport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminAirportMapper {


    List<Airport> selectAllAirports();


    void insert(Airport airport);


    Airport getByName(String name);

    void update(Airport airport);

    void deleteById(Integer id);

    List<Airport> search(@Param("name") String name,
                         @Param("city") String city,
                         @Param("address") String address,
                         @Param("offset") int offset,
                         @Param("size") int size);

    long countAll();
    List<Airport> selectAllPaged(@Param("offset") int offset,
                                 @Param("size") int size);
}
