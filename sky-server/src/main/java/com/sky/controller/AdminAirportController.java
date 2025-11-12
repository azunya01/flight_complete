package com.sky.controller;

import com.sky.entity.Airport;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AdminAirportService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
@Api(tags="管理员机场相关接口")
public class AdminAirportController {
    //机场的增删改查
    @Autowired
    private AdminAirportService adminAirportService;
    @PostMapping("/airport/add")
    public Result<Airport> AddAirport(@RequestBody Airport airport){
        log.info("新增机场: {}", airport);
        Airport airportRes=adminAirportService.addAirport(airport);
        return Result.success(airportRes);
    }

    //删除
    @GetMapping("/airport/delete/{id}")
    public Result<Integer> deleteAirport(@PathVariable Integer id){
        log.info("删除机场: {}", id);
        adminAirportService.deleteAirport(id);
        return Result.success(id);
    }
    //修改
    @PostMapping("/airport/update")
    public Result<Airport> updateAirport(@RequestBody Airport airport) {
        log.info("修改机场: {}", airport);
         Airport airportRes = adminAirportService.updateAirport(airport);
        return Result.success(airportRes);
    }
    //查询
    //查询所有机场
    // 查询所有机场（分页）
    @GetMapping("/airport/list")
    public Result<PageResult<Airport>> getAllAirports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("查询所有机场 page={}, size={}", page, size);
        PageResult<Airport> pr = adminAirportService.getAllAirports(page, size);
        log.info("机场列表: {}", pr);
        return Result.success(pr);
    }
    //查询部分
    /**
     * 按可选条件查询机场列表：name / city / address 任意组合都可
     * 分页可选：page(从1开始)、size(默认10)
     */
    @GetMapping("/airports")
    public Result<List<Airport>> searchAirports(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String address,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        List<Airport> list = adminAirportService.searchAirports(name, city, address, page, size);
        return Result.success(list);
    }
}

