package com.sky.service;

import com.sky.entity.SeatType;
import com.sky.mapper.AdminSeatTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSeatTypeService {

    private final AdminSeatTypeMapper adminSeatTypeMapper;

    public SeatType add(SeatType st) {
        // 友好校验（可选）
        if (st.getCode() != null && adminSeatTypeMapper.getByCode(st.getCode()) != null) {
            throw new RuntimeException("舱位代码已存在");
        }
        if (st.getName() != null && adminSeatTypeMapper.getByName(st.getName()) != null) {
            throw new RuntimeException("舱位名称已存在");
        }
        try {
            adminSeatTypeMapper.insert(st); // 自增主键回填到 st.id
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("舱位类型重复（code/name 唯一约束冲突）", e);
        }
        return st;
    }

    public SeatType update(SeatType st) {
        if (st.getId() == null) throw new RuntimeException("缺少 id");
        // 可选：校验目标是否存在
        if (adminSeatTypeMapper.getById(st.getId()) == null) throw new RuntimeException("舱位不存在");
        try {
            adminSeatTypeMapper.update(st);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("舱位类型重复（code/name 唯一约束冲突）", e);
        }
        return st;
    }

    public void delete(Integer id) {
        adminSeatTypeMapper.deleteById(id);
    }

    public SeatType getById(Integer id) {
        return adminSeatTypeMapper.getById(id);
    }

    public List<SeatType> listAll() {
        return adminSeatTypeMapper.selectAll();
    }
}
