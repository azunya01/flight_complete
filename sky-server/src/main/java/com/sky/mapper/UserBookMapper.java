package com.sky.mapper;

import com.sky.dto.PassengerDTO;
import com.sky.dto.SeatOption;
import com.sky.vo.PassengerVO;
import com.sky.vo.UserFlightDetailVO;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserBookMapper {

    UserFlightDetailVO getFlightDetailById(Long flightId);
    String getFlightNameById(@Param("flightId") Integer flightId);

    // 自动择舱：列出该航班所有舱位（按价格升序）
    List<SeatOption> listSeatOptions(@Param("flightId") Integer flightId);

    // 取某舱位单价
    BigDecimal getSeatTypePrice(@Param("flightId") Integer flightId,
                                @Param("seatTypeId") Integer seatTypeId);

    // 原子扣减 available
    int decreaseAvailable(@Param("flightId") Integer flightId,
                          @Param("seatTypeId") Integer seatTypeId,
                          @Param("count") Integer count);

    // 插入订单头
    int insertOrder(@Param("userId") String userId,
                    @Param("flightId") Integer flightId,
                    @Param("status") Integer status,
                    @Param("totalAmount") BigDecimal totalAmount);

    Long lastInsertId();

    // 批量插入订单明细（仅 passenger_id）
    int insertOrderItems(@Param("orderId") Long orderId,
                         @Param("flightId") Integer flightId,
                         @Param("seatTypeId") Integer seatTypeId,
                         @Param("price") BigDecimal price,
                         @Param("items") List<PassengerDTO> passengers);
    List<PassengerVO> getPassengersByIds(@Param("ids") List<Integer> ids);
    Long findPassengerIdByUserAndIdNo(@Param("userId") String userId, @Param("idNo") String idNo);

    int insertPassengerFromSnapshot(@Param("userId") String userId,
                                    @Param("name") String name,
                                    @Param("gender") String gender,
                                    @Param("idNo") String idNo,
                                    @Param("phone") String phone);

}
