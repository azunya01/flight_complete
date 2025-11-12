// com.sky.mapper.UserFlightSearchMapper
package com.sky.mapper;

import com.sky.vo.UserFlightVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserFlightSearchMapper {

    long countByCitiesAndDate(@Param("fromCity") String fromCity,
                              @Param("toCity") String toCity,
                              @Param("start") LocalDateTime startInclusive,
                              @Param("end") LocalDateTime endExclusive);

    List<UserFlightVO> selectByCitiesAndDate(@Param("fromCity") String fromCity,
                                             @Param("toCity") String toCity,
                                             @Param("start") LocalDateTime startInclusive,
                                             @Param("end") LocalDateTime endExclusive,
                                             @Param("offset") int offset,
                                             @Param("size") int size);
    List<String> listCities(@Param("keyword") String keyword);
}
