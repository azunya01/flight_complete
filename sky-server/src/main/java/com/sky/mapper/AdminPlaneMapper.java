package com.sky.mapper;

import com.sky.entity.Plane;
import com.sky.entity.PlaneSeatCapacity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminPlaneMapper {

    // 基础 CRUD
    void insert(Plane plane);
    void update(Plane plane);
    void deleteById(@Param("id") Integer id);
    Plane getById(@Param("id") Integer id);
    Plane getByName(@Param("name") String name);

    // 全量分页
    long countAll();
    List<Plane> selectAllPaged(@Param("offset") int offset, @Param("size") int size);

    // 条件分页（按 name 模糊）
    long countByName(@Param("name") String name);
    List<Plane> selectByNamePaged(@Param("name") String name,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    void insertPlaneSeatType(Integer planeId, Integer seatTypeId, Integer capacity, Integer rowNumber);

    void delectPlaneSeatTypesByPlaneId(Integer id);

    void updatePlaneSeatType(Integer planeId, Integer seatTypeId, Integer capacity,Integer rowNumber);


    List<PlaneSeatCapacity> getPlaneSeatCapacityByPlaneId(Integer planeId);
    // —— 座位模板相关 —— //
    int upsertPlaneSeatType(@Param("planeId") Integer planeId,
                            @Param("seatTypeId") Integer seatTypeId,
                            @Param("capacity") Integer capacity,
                            @Param("rowNumber") Integer rowNumber);

    int deletePlaneSeatTypesNotIn(@Param("planeId") Integer planeId,
                                  @Param("seatTypeIds") List<Integer> seatTypeIds);

    // 兜底：清空该飞机的所有模板行（可选）
    int deleteAllPlaneSeatTypes(@Param("planeId") Integer planeId);
}
