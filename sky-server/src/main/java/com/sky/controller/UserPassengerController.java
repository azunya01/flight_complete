// com.sky.controller.UserPassengerController
package com.sky.controller;

import com.sky.dto.PassengerCreateDTO;
import com.sky.dto.PassengerUpdateDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.UserPassengerService;
import com.sky.utils.UserContext;
import com.sky.vo.PassengerVO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/passengers")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "用户常用乘客相关接口")
public class UserPassengerController {

    private final UserPassengerService service;

    // 新增常用乘客
    @PostMapping
    public Result<PassengerVO> add(@RequestBody PassengerCreateDTO dto) {

        dto.setUserId(Math.toIntExact(UserContext.getUserId()));
        log.info("创建常用乘客: userId={}, dto={}", UserContext.getUserId(), dto);
        return Result.success(service.add(dto));
    }

    // 更新常用乘客
    @PutMapping("/{id}")
    public Result<PassengerVO> update(@PathVariable Long id, @RequestBody PassengerUpdateDTO dto) {
        dto.setUserId(Math.toIntExact(UserContext.getUserId()));
        dto.setId(id);
        log.info("Update passenger id={} user={}", id, dto.getUserId());
        return Result.success(service.update(dto));
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Integer userId = Math.toIntExact(UserContext.getUserId());
        log.info("Delete passenger id={} user={}", id, userId);
        service.delete(id, String.valueOf(userId));
        return Result.success();
    }

    // 详情
    @GetMapping("/{id}")
    public Result<PassengerVO> get(@PathVariable Long id) {
        Integer userId = Math.toIntExact(UserContext.getUserId());
        return Result.success(service.get(id, String.valueOf(userId)));
    }

    // 列表（可搜索）
    @GetMapping
    public Result<PageResult<PassengerVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Integer userId = Math.toIntExact(UserContext.getUserId());
        return Result.success(service.list(String.valueOf(userId), keyword, page, size));
    }
}
