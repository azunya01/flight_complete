// com.sky.service.UserOrderService
package com.sky.service;

import com.sky.entity.OrderPassengerRow;
import com.sky.mapper.UserOrderMapper;
import com.sky.result.PageResult;
import com.sky.vo.OrderVO;
import com.sky.vo.PassengerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserOrderService {

    private final UserOrderMapper orderMapper;

    /**
     * 取消订单：把订单下的所有票回补库存，然后把订单状态设为 2(已取消)
     * 不考虑并发，按最简单流程写。
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        var base = orderMapper.getOrderBase(orderId);
        if (base == null) throw new IllegalArgumentException("订单不存在: " + orderId);
        if (base.getStatus() != 1) { // 只处理已支付/确认的
            orderMapper.updateOrderStatus(orderId, 2);
            return;
        }

        var seatTypeIds = orderMapper.listSeatTypeIds(orderId);
        for (Integer seatTypeId : seatTypeIds) {
            orderMapper.increaseAvailable(base.getFlightId(), seatTypeId);
        }

        orderMapper.updateOrderStatus(orderId, 2);
    }


    // com.sky.service.UserOrderService
    public PageResult<OrderVO> listOrders(String userId, Integer page, Integer size) {
        if (userId == null || userId.isBlank()) {
            return new PageResult<>(0, List.of(), 1, size == null ? 10 : size);
        }
        int p = Math.max(1, page == null ? 1 : page);
        int s = Math.min(Math.max(1, size == null ? 10 : size), 100);
        int offset = (p - 1) * s;

        long total = orderMapper.countOrdersByUser(userId);
        if (total == 0) {
            return new PageResult<>(0, List.of(), p, s);
        }

        // 1) 分页取订单头（已拼好 from/to、seatTypeId 代表、total_amount）
        List<OrderVO> orders = orderMapper.listOrderHeadersPaged(userId, offset, s);

        // 2) 批量拉取乘客
        List<Long> orderIds = orders.stream().map(OrderVO::getOrderId).toList();
        List<OrderPassengerRow> rows = orderMapper.listPassengersByOrderIds(orderIds);

        // 3) 聚合乘客
        Map<Long, List<PassengerVO>> map = rows.stream()
                .collect(Collectors.groupingBy(
                        OrderPassengerRow::getOrderId,
                        Collectors.mapping(OrderPassengerRow::toVO, Collectors.toList())
                ));

        // 4) 组装
        for (OrderVO vo : orders) {
            vo.setPassengers(map.getOrDefault(vo.getOrderId(), List.of()));
        }
        return new PageResult<>(total, orders, p, s);
    }

}
