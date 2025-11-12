package com.sky.controller;


import com.sky.dto.FlightDTO;
import com.sky.dto.FlightUpdateDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AdminFlightService;
import com.sky.vo.FlightVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//航班的增删改查
@RestController
@RequestMapping("/admin")
@Slf4j
@Api(tags = "管理员航班相关接口")
public class AdminFlightController {
    @Autowired
    private AdminFlightService adminFlightService;

    //增加航班
    @PostMapping("/flight/add")
    public Result<FlightVO> addFlight(@RequestBody FlightDTO flightDTO) {
        log.info("新增航班: {}", flightDTO);
        FlightVO flightVO = adminFlightService.addFlight(flightDTO);
        return Result.success(flightVO);
    }

    //删除航班
    @PostMapping("/flight/delete/{id}")
    public Result<Integer> deleteFlight(@PathVariable Integer id) {
        log.info("删除航班: {}", id);
        adminFlightService.deleteFlight(id);
        return Result.success(id);
    }

    //修改航班
    @PostMapping("/flight/update")
    public Result<FlightVO> updateFlight(@RequestBody FlightUpdateDTO flightUpdateDTO) {
        log.info("修改航班: {}", flightUpdateDTO);
        FlightVO flightVO = adminFlightService.updateFlight(flightUpdateDTO);
        return Result.success(flightVO);
    }
  //航班列表
  @GetMapping("/flight/list")
  public Result<PageResult<FlightVO>> getAllAirlines(
          @RequestParam(defaultValue = "1") Integer page,
          @RequestParam(defaultValue = "10") Integer size
  ){
      log.info("查询所有航班 page={}, size={}", page, size);
      PageResult<FlightVO> pr = adminFlightService.getAllAirlines(page, size);
      log.info("航线列表分页: total={}, page={}, size={}", pr.getTotal(), pr.getPage(), pr.getSize());
      return Result.success(pr);
  }
  @GetMapping("/flight/{id}")
  public Result<FlightVO> getFlightById(@PathVariable Integer id) {
        log.info("查询航班: {}", id);
        FlightVO flightVO = adminFlightService.getFlightById(id);
        return Result.success(flightVO);
  }
}
