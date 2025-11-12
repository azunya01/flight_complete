package com.sky.service;

import com.sky.entity.Airport;
import com.sky.mapper.AdminAirportMapper;
import com.sky.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAirportService {

    @Autowired
    private AdminAirportMapper adminAirportMapper;

    public Airport addAirport(Airport airport) {
        Airport newAirport = adminAirportMapper.getByName(airport.getName());
        if (newAirport != null) {
            throw new RuntimeException("机场已存在");
        }
        adminAirportMapper.insert(airport);
        return airport;
    }

    public void deleteAirport(Integer id) {
        adminAirportMapper.deleteById(id);
    }

    public Airport updateAirport(Airport airport) {
        adminAirportMapper.update(airport);
        return airport;
    }

    public PageResult<Airport> getAllAirports(int page, int size) {
        // 安全兜底
        page = Math.max(1, page);
        size = Math.min(Math.max(1, size), 100); // 每页最多100，可按需调整

        int offset = (page - 1) * size;
        long total = adminAirportMapper.countAll();
        List<Airport> records = adminAirportMapper.selectAllPaged(offset, size);
        return new PageResult<>(total, records, page, size);
    }

    public List<Airport> searchAirports(String name, String city, String address, Integer page, Integer size) {
        // 简单的分页偏移量计算（page 从 1 开始）
        int offset = Math.max(0, (page - 1) * size);
        return adminAirportMapper.search(name, city, address, offset, size);
    }
}
