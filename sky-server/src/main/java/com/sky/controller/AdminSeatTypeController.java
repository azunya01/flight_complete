package com.sky.controller;

import com.sky.entity.SeatType;
import com.sky.result.Result;
import com.sky.service.AdminSeatTypeService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
@Api(tags = "管理员-舱位类型接口")
public class AdminSeatTypeController {

    @Autowired
    private AdminSeatTypeService adminSeatTypeService;

    @PostMapping("/seatType/add")
    public Result<SeatType> add(@RequestBody SeatType st) {
        log.info("新增舱位类型: {}", st);
        return Result.success(adminSeatTypeService.add(st));
    }

    @PostMapping("/seatType/update")
    public Result<SeatType> update(@RequestBody SeatType st) {
        log.info("修改舱位类型: {}", st);
        return Result.success(adminSeatTypeService.update(st));
    }

    @GetMapping("/seatType/delete/{id}")
    public Result<Integer> delete(@PathVariable Integer id) {
        log.info("删除舱位类型: {}", id);
        adminSeatTypeService.delete(id);
        return Result.success(id);
    }

    @GetMapping("/seatType/{id}")
    public Result<SeatType> getOne(@PathVariable Integer id) {
        return Result.success(adminSeatTypeService.getById(id));
    }

    @GetMapping("/seatType/list")
    public Result<List<SeatType>> listAll() {
        return Result.success(adminSeatTypeService.listAll());
    }
}
