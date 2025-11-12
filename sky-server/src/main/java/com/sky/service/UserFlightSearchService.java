// com.sky.service.UserFlightSearchService
package com.sky.service;

import com.sky.mapper.UserFlightSearchMapper;
import com.sky.result.PageResult;
import com.sky.vo.UserFlightVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFlightSearchService {

    private final UserFlightSearchMapper userFlightSearchMapper;

    public PageResult<UserFlightVO> search(String fromCity,
                                           String toCity,
                                           LocalDate date,
                                           Integer page,
                                           Integer size) {
        int p = Math.max(1, page == null ? 1 : page);
        int s = Math.min(Math.max(1, size == null ? 10 : size), 100);
        int offset = (p - 1) * s;

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = date.plusDays(1).atStartOfDay();

        long total = userFlightSearchMapper.countByCitiesAndDate(fromCity, toCity, start, end);
        List<UserFlightVO> rows=userFlightSearchMapper.selectByCitiesAndDate(fromCity, toCity, start, end, offset, s);
        log.info("Search flights: {} -> {}, date={}, total={}, page={}, size={}",
                fromCity, toCity, date, total, p, s );

        return new PageResult<>(total, rows, p, s);
    }
    public List<String> list(String keyword) {
        return userFlightSearchMapper.listCities(keyword);
    }
}
