// com.sky.mapper.UserOrderMapper
package com.sky.mapper;

import com.sky.entity.OrderBase;
import com.sky.entity.OrderPassengerRow;
import com.sky.vo.OrderVO;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserOrderMapper {

    // 取订单基础信息（只要 flight_id 和 status）
    OrderBase getOrderBase(@Param("orderId") Long orderId);

    // 列出订单下所有 seat_type_id（一行一条，不聚合也行）
    List<Integer> listSeatTypeIds(@Param("orderId") Long orderId);

    // 回补库存：available += 1
    int increaseAvailable(@Param("flightId") Integer flightId,
                          @Param("seatTypeId") Integer seatTypeId);

    // 改订单状态
    int updateOrderStatus(@Param("orderId") Long orderId,
                          @Param("status") Integer status);

    /**
     * 订单头 + 航班/航线信息 + 代表 seatTypeId（若多舱取最小）
     */
    List<OrderVO> listOrderHeaders(@Param("userId") String userId);

    /**
     * 批量查询订单的乘客列表
     */
    List<OrderPassengerRow> listPassengersByOrderIds(@Param("orderIds") List<Long> orderIds);

    /* 将联表行转为 PassengerVO 的简单载体 */
    /** 分页的订单头（含 flight/airline、代表 seatTypeId、total_amount=整单金额） */
    List<OrderVO> listOrderHeadersPaged(@Param("userId") String userId,
                                        @Param("offset") int offset,
                                        @Param("size") int size);
    long countOrdersByUser(@Param("userId") String userId);


}