package com.sky.service;

import com.sky.entity.Airline;
import com.sky.mapper.AdminAirlineMapper;
import com.sky.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAirlineService {
    @Autowired
    private AdminAirlineMapper adminMapper;


    public Airline addAirline(Airline airline) {
        Airline newAirline = adminMapper.getByFromTo(airline.getFromAirport(), airline.getToAirport());
        if (newAirline != null) {
            throw new RuntimeException("航线已存在");
        }
        adminMapper.insert(airline);
        return airline;

    }

    public void deleteAirline(Integer airlineId) {
        adminMapper.delectAirlineById(airlineId);
    }

    public Airline updateAirline(Airline airline) {
        adminMapper.update(airline);
        return airline;
    }

    public Airline getAirline(Integer airlineId) {
        return adminMapper.getById(airlineId);
    }

    public PageResult<Airline> getAllAirlines(int page, int size) {
        page = Math.max(1, page);
        size = Math.min(Math.max(1, size), 100);
        int offset = (page - 1) * size;

        long total = adminMapper.countAll();
        List<Airline> rows = adminMapper.selectAllPaged(offset, size);
        return new PageResult<>(total, rows, page, size);
    }

    public PageResult<Airline> getAirlinesByFromTo(String fromAirport, String toAirport, int page, int size) {
        page = Math.max(1, page);
        size = Math.min(Math.max(1, size), 100);
        int offset = (page - 1) * size;

        long total = adminMapper.countByFromTo(fromAirport, toAirport);
        List<Airline> rows = adminMapper.selectByFromToPaged(fromAirport, toAirport, offset, size);
        return new PageResult<>(total, rows, page, size);
    }
}
