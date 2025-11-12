package com.sky.mapper;

import com.sky.dto.PassengerCreateDTO;
import com.sky.dto.PassengerUpdateDTO;
import com.sky.vo.PassengerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface UserPassengerMapper {

    int insert(PassengerCreateDTO dto);

    int update(PassengerUpdateDTO dto);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") String userId);

    PassengerVO getByIdAndUser(@Param("id") Long id, @Param("userId") String userId);

    // 新增：按 (userId, idNo) 查询
    PassengerVO getByUserAndIdNo(@Param("userId") Integer userId, @Param("idNo") String idNo);

    long countByUserAndKeyword(@Param("userId") String userId, @Param("kw") String keyword);

    List<PassengerVO> selectByUserAndKeywordPaged(@Param("userId") String userId,
                                                  @Param("kw") String keyword,
                                                  @Param("offset") int offset,
                                                  @Param("size") int size);
}
