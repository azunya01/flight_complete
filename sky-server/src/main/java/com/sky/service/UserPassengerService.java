// com.sky.service.UserPassengerService
package com.sky.service;

import com.sky.dto.PassengerCreateDTO;
import com.sky.dto.PassengerUpdateDTO;
import com.sky.mapper.UserPassengerMapper;
import com.sky.result.PageResult;
import com.sky.vo.PassengerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPassengerService {
    @Autowired
    private final UserPassengerMapper mapper;

    public PassengerVO add(PassengerCreateDTO dto) {
        try {
            mapper.insert(dto);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("该证件号已在该用户下存在");
        }
        // 用唯一键 (user_id, id_no) 回查，无需 dto.getId()
        return mapper.getByUserAndIdNo(dto.getUserId(), dto.getIdNo());
    }


    public PassengerVO update(PassengerUpdateDTO dto) {
        int n = mapper.update(dto);
        if (n == 0) throw new RuntimeException("乘客不存在或不属于该用户");
        return mapper.getByIdAndUser(dto.getId(), String.valueOf(dto.getUserId()));
    }

    public void delete(Long id, String userId) {
        int n = mapper.deleteByIdAndUser(id, userId);
        if (n == 0) throw new RuntimeException("乘客不存在或不属于该用户");
    }

    public PassengerVO get(Long id, String userId) {
        PassengerVO vo = mapper.getByIdAndUser(id, userId);
        if (vo == null) throw new RuntimeException("乘客不存在或不属于该用户");
        return vo;
    }

    public PageResult<PassengerVO> list(String userId, String keyword, Integer page, Integer size) {
        int p = Math.max(1, page == null ? 1 : page);
        int s = Math.min(Math.max(1, size == null ? 10 : size), 100);
        int offset = (p - 1) * s;

        long total = mapper.countByUserAndKeyword(userId, keyword);
        List<PassengerVO> rows = total == 0 ? List.of()
                : mapper.selectByUserAndKeywordPaged(userId, keyword, offset, s);
        return new PageResult<>(total, rows, p, s);
    }
}
