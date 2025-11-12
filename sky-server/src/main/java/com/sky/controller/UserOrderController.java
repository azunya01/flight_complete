// com.sky.controller.UserOrderController
package com.sky.controller;

import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.UserOrderService;
import com.sky.utils.UserContext;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/orders")
@RequiredArgsConstructor
@Api("用户订单相关接口")
@Slf4j
public class UserOrderController {

    private final UserOrderService userOrderService;

    // com.sky.controller.UserOrderController
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        log.info("List orders for userId={}, page={}, size={}", userId, page, size);
        PageResult<OrderVO> pr = userOrderService.listOrders(String.valueOf(userId), page, size);
        log.info("pr:{}", pr);
        return Result.success(
                pr);
    }


    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable Long orderId) {
        userOrderService.cancelOrder(orderId);
        return Result.success();
    }


}
