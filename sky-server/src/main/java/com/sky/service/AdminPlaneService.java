package com.sky.service;

import com.sky.dto.PlaneDTO;
import com.sky.entity.PlaneSeatCapacity;
import com.sky.entity.SeatTypeCounts;
import com.sky.entity.Plane;
import com.sky.mapper.AdminPlaneMapper;
import com.sky.mapper.AdminSeatTypeMapper;
import com.sky.result.PageResult;
import com.sky.vo.PlaneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminPlaneService {

    @Autowired
    private AdminSeatTypeMapper adminSeatTypeMapper;
    @Autowired
    private AdminPlaneMapper adminPlaneMapper;



    @Transactional
    public Plane addPlane(PlaneDTO planeDTO) {
        // 友好校验（可选）
        Plane exist = adminPlaneMapper.getByName(planeDTO.getPlaneName());
        if (exist != null) throw new RuntimeException("飞机已存在");

        Plane plane = new Plane();
        plane.setName(planeDTO.getPlaneName());
        try {
            adminPlaneMapper.insert(plane);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("飞机已存在", e);
        }

        List<SeatTypeCounts> seatTypeCounts = planeDTO.getSeatTypeCounts();
        if (seatTypeCounts != null && !seatTypeCounts.isEmpty()) {
            Integer planeId = plane.getId();
            seatTypeCounts.forEach(seatTypeCountsDTO -> {
                Integer seatTypeId=adminSeatTypeMapper.getByName(seatTypeCountsDTO.getSeatTypeName()).getId();
                Integer count=seatTypeCountsDTO.getCount();
                Integer rowNumber=seatTypeCountsDTO.getRowNumber();
                adminPlaneMapper.insertPlaneSeatType(planeId,seatTypeId,count,rowNumber);
            });
        }


        return plane;
    }

    @Transactional
    public void deletePlane(Integer id) {
        adminPlaneMapper.deleteById(id);
        adminPlaneMapper.delectPlaneSeatTypesByPlaneId(id);
    }

    @Transactional
    public Plane updatePlane(PlaneDTO planeDTO) {
        // 1) 查 planeId（更稳建议 DTO 直接带 id；否则 getByName 要保证唯一）
        Plane plane = adminPlaneMapper.getByName(planeDTO.getPlaneName());
        if (plane == null) {
            throw new RuntimeException("飞机不存在: " + planeDTO.getPlaneName());
        }
        Integer planeId = plane.getId();

        // 2) 更新飞机名称（如允许改名）
        Plane toUpdate = new Plane();
        toUpdate.setId(planeId);
        toUpdate.setName(planeDTO.getPlaneName());
        adminPlaneMapper.update(toUpdate);

        // 3) 座位模板：UPSERT + 清理
        List<SeatTypeCounts> list = planeDTO.getSeatTypeCounts();
        if (list != null) {
            // 本次提交出现的 seatTypeId 列表，用于后续“删除缺失”
            List<Integer> submittedSeatTypeIds = new ArrayList<>(list.size());

            for (SeatTypeCounts stc : list) {
                // 3.1 解析 seatTypeId
                Integer seatTypeId = adminSeatTypeMapper.getByName(stc.getSeatTypeName()).getId();
                submittedSeatTypeIds.add(seatTypeId);

                // 3.2 UPSERT（新增或更新）
                Integer cap = stc.getCount();
                Integer row = stc.getRowNumber();
                adminPlaneMapper.upsertPlaneSeatType(planeId, seatTypeId, cap, row);
            }

            // 3.3 删除“未在本次提交中出现”的旧行（实现真正的覆盖更新）
            adminPlaneMapper.deletePlaneSeatTypesNotIn(planeId, submittedSeatTypeIds);
        }

        return toUpdate;
    }


    public Plane getPlane(Integer id) {
        return adminPlaneMapper.getById(id);
    }

    public PageResult<Plane> getAllPlanes(int page, int size) {
        page = Math.max(1, page);
        size = Math.min(Math.max(1, size), 100);
        int offset = (page - 1) * size;

        long total = adminPlaneMapper.countAll();
        List<Plane> rows = adminPlaneMapper.selectAllPaged(offset, size);
        return new PageResult<>(total, rows, page, size);
    }

    public PageResult<Plane> searchPlanes(String name, int page, int size) {
        page = Math.max(1, page);
        size = Math.min(Math.max(1, size), 100);
        int offset = (page - 1) * size;

        long total = adminPlaneMapper.countByName(name);
        List<Plane> rows = adminPlaneMapper.selectByNamePaged(name, offset, size);
        return new PageResult<>(total, rows, page, size);
    }

    public PlaneVO getPlaneDetail(Integer id) {
        Plane plane = adminPlaneMapper.getById(id);
        PlaneVO planeVO = new PlaneVO();
        List<PlaneSeatCapacity>  planeSeatCapacities= adminPlaneMapper.getPlaneSeatCapacityByPlaneId(id);
        if (planeSeatCapacities != null && !planeSeatCapacities.isEmpty()) {
            List<SeatTypeCounts> seatTypeCounts =new ArrayList<>();
            for (PlaneSeatCapacity psc : planeSeatCapacities) {
                String seatTypeName=adminSeatTypeMapper.getById(psc.getSeatTypeId()).getName();
                Integer count=psc.getCapacity();
                SeatTypeCounts stc=new SeatTypeCounts();
                stc.setSeatTypeName(seatTypeName);
                stc.setCount(count);
                stc.setRowNumber(psc.getRowNumber());
                seatTypeCounts.add(stc);
            }
            planeVO.setSeatTypeCounts(seatTypeCounts);
        }

        planeVO.setId(plane.getId());
        planeVO.setPlaneName(plane.getName());
        return planeVO;
    }
}
