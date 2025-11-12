// com.sky.controller.UserFlightSearchController
package com.sky.controller;

import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.UserFlightSearchService;
import com.sky.vo.UserFlightVO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/user/flights")
@Slf4j
@RequiredArgsConstructor
@Api(tags = "用户航班搜索相关接口")
public class UserFlightSearchController {

    private final UserFlightSearchService userFlightSearchService;

    @GetMapping("/search")
    public Result<PageResult<UserFlightVO>> search(
            @RequestParam String fromCity,
            @RequestParam String toCity,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("User search by cities: {} -> {}, date={}, page={}, size={}",
                fromCity, toCity, date, page, size);
        PageResult<UserFlightVO> flights = userFlightSearchService.search(fromCity, toCity, date, page, size);
        log.info("User flights: {}", flights);
        return Result.success(flights);
    }
    @GetMapping("/cities")
    public Result<List<String>> list(@RequestParam(required = false) String keyword) {
        log.info("List cities, keyword={}", keyword);
        return Result.success(userFlightSearchService.list(keyword));
    }
}
