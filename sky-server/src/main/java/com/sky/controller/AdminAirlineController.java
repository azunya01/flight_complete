package com.sky.controller;


import com.sky.entity.Airline;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AdminAirlineService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
@Api(tags="管理员航线管理相关接口")
public class AdminAirlineController {
    @Autowired
    private AdminAirlineService adminService;
    //航线增删改查
    //增加
    @PostMapping("/airline/add")
    public Result<Airline> addAirline(@RequestBody Airline airline){
        log.info("新增航线: {}", airline);
        Airline airlineRes=adminService.addAirline(airline);
        return Result.success(airlineRes);
    }
    //删除
    @GetMapping("/airline/delete/{airlineId}")
    public Result<Integer> deleteAirline(@PathVariable Integer airlineId){
        log.info("删除航线: {}", airlineId);
        adminService.deleteAirline(airlineId);
        return Result.success(airlineId);
    }
    //改
    @PostMapping("/airline/update")
    public Result<Airline> updateAirline(@RequestBody Airline airline) {
        log.info("修改航线: {}", airline);
        Airline airlineRes = adminService.updateAirline(airline);
        return Result.success(airlineRes);
    }
    //查
    //查一个
    @GetMapping("/airline/{airlineId}")
    public Result<Airline> getAirline(@PathVariable Integer airlineId){
        log.info("查询航线: {}", airlineId);
        Airline n=adminService.getAirline(airlineId);
        return Result.success(n);
    }
    // 查所有航线（分页）
    @GetMapping("/airline/list")
    public Result<PageResult<Airline>> getAllAirlines(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ){
        log.info("查询所有航线 page={}, size={}", page, size);
        PageResult<Airline> pr = adminService.getAllAirlines(page, size);
        log.info("航线列表分页: total={}, page={}, size={}", pr.getTotal(), pr.getPage(), pr.getSize());
        return Result.success(pr);
    }

    // 根据出发地和目的地查询航线（分页）
    @GetMapping("/airline")
    public Result<PageResult<Airline>> getAirlinesByFromTo(
            @RequestParam String fromAirport,
            @RequestParam String toAirport,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ){
        log.info("按出发/目的地查航线: {} -> {} (page={}, size={})", fromAirport, toAirport, page, size);
        PageResult<Airline> pr = adminService.getAirlinesByFromTo(fromAirport, toAirport, page, size);
        return Result.success(pr);
    }
}
