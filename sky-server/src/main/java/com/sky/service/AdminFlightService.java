package com.sky.service;

import com.sky.dto.FlightDTO;
// 如果你前端仍传这个，就兼容一下
import com.sky.dto.FlightSeatInventoryDTO;
import com.sky.dto.FlightUpdateDTO;
import com.sky.entity.Flight;
import com.sky.entity.Plane;
import com.sky.entity.PlaneSeatCapacity;
import com.sky.entity.FlightSeatInventory;
import com.sky.mapper.AdminFlightMapper;
import com.sky.mapper.AdminPlaneMapper;
import com.sky.result.PageResult;
import com.sky.vo.FlightSeatVO;
import com.sky.vo.FlightVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminFlightService {

    @Autowired
    private final AdminFlightMapper adminFlightMapper;
    @Autowired
    private final AdminPlaneMapper adminPlaneMapper;

    /**
     * 新增航班：只需要 seatTypeId -> price
     * total/available 从 plane_seat_capacity 拷贝
     */
    @Transactional(rollbackFor = Exception.class)
    public FlightVO addFlight(FlightDTO dto) {
        // 1) 航班名唯一
        if (adminFlightMapper.getFlightByFlightName(dto.getName()) != null) {
            throw new RuntimeException("航班已存在");
        }
        // 2) 时间合法
        LocalDateTime dep = dto.getDepartureTime();
        LocalDateTime arr = dto.getArrivalTime();
        if (dep == null || arr == null || !arr.isAfter(dep)) {
            throw new RuntimeException("起飞/降落时间非法");
        }

        // 3) 插入 flight（回填自增ID）
        Flight flight = new Flight();
        BeanUtils.copyProperties(dto, flight);
        adminFlightMapper.insertFlight(flight);
        Integer flightId = flight.getId();

        // 4) 读取飞机模板
        List<PlaneSeatCapacity> caps =
                adminPlaneMapper.getPlaneSeatCapacityByPlaneId(dto.getPlaneId());
        if (caps == null || caps.isEmpty()) {
            throw new RuntimeException("该飞机未配置舱位容量模板");
        }

        // 5) 组装价格映射（兼容两种 DTO：seatPrices 或 flightSeatDTOS）
        Map<Integer, BigDecimal> priceMap = new HashMap<>();
        if (dto.getSeatPrices() != null) {
            dto.getSeatPrices().forEach(p -> {
                if (p.getSeatTypeId() != null && p.getPrice() != null) {
                    priceMap.put(p.getSeatTypeId(), p.getPrice());
                }
            });
        }
        // 强制所有模板舱位都提供价格（也可放宽为默认 0）
        for (PlaneSeatCapacity c : caps) {
            if (!priceMap.containsKey(c.getSeatTypeId())) {
                throw new RuntimeException("缺少舱位 " + c.getSeatTypeId() + " 的价格");
            }
        }

        // 6) 生成库存行：total=available=capacity，price=管理员输入
        List<FlightSeatInventory> rows = new ArrayList<>(caps.size());
        for (PlaneSeatCapacity c : caps) {
            FlightSeatInventory inv = new FlightSeatInventory();
            inv.setFlightId(flightId);
            inv.setSeatTypeId(c.getSeatTypeId());
            inv.setTotal(c.getCapacity());
            inv.setAvailable(c.getCapacity());
            inv.setPrice(priceMap.get(c.getSeatTypeId()));
            rows.add(inv);
        }
        adminFlightMapper.batchInsertInventory(rows);

        // 7) 组装 VO：座位明细直接用联查 seat_type 的结果映射到 FlightSeatVO
        List<FlightSeatVO> seatVOS = adminFlightMapper.listSeatViewByFlightId(flightId);

        // 可选：查飞机名（你已有 AdminPlaneMapper.getById）
        Plane plane = adminPlaneMapper.getById(dto.getPlaneId());

        return getFlightVO(flight, seatVOS, plane);
    }

    private FlightVO getFlightVO(Flight flight, List<FlightSeatVO> seatVOS, Plane plane) {
        FlightVO vo = new FlightVO();
        vo.setId(flight.getId());
        vo.setName(flight.getName());
        vo.setPlaneName(plane != null ? plane.getName() : null);
        vo.setDepartureTime(flight.getDepartureTime());
        vo.setArrivalTime(flight.getArrivalTime());
        vo.setFlightSeatVOS(seatVOS);
        return vo;
    }

    public void deleteFlight(Integer id) {
        adminFlightMapper.deleteFlightById(id);
        adminFlightMapper.deleteInventoryByFlightId(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public FlightVO updateFlight(FlightUpdateDTO dto) {
        // 0) 基本校验
        if (dto.getId() == null) throw new RuntimeException("缺少航班ID");
        Flight db = adminFlightMapper.getFlightById(dto.getId());
        if (db == null) throw new RuntimeException("航班不存在");

        // 航班名唯一（排除自己）
        if (dto.getName() != null && adminFlightMapper.countByNameExcludingId(dto.getName(), dto.getId()) > 0) {
            throw new RuntimeException("航班名称已存在");
        }
        // 时间合法
        LocalDateTime dep = dto.getDepartureTime() != null ? dto.getDepartureTime() : db.getDepartureTime();
        LocalDateTime arr = dto.getArrivalTime() != null ? dto.getArrivalTime() : db.getArrivalTime();
        if (dep == null || arr == null || !arr.isAfter(dep)) {
            throw new RuntimeException("起飞/降落时间非法");
        }

        // 1) 更新 flight 基本信息
        Flight update = new Flight();
        BeanUtils.copyProperties(db, update); // 先带上原值
        if (dto.getName() != null) update.setName(dto.getName());
        if (dto.getPlaneId() != null) update.setPlaneId(dto.getPlaneId());
        if (dto.getAirlineId() != null) update.setAirlineId(dto.getAirlineId());
        if (dto.getDepartureTime() != null) update.setDepartureTime(dto.getDepartureTime());
        if (dto.getArrivalTime() != null) update.setArrivalTime(dto.getArrivalTime());
        adminFlightMapper.updateFlight(update);

        Integer flightId = update.getId();
        boolean planeChanged = !Objects.equals(db.getPlaneId(), update.getPlaneId());

        // 2) 组织前端覆盖（seatTypeId -> (price, available)）
        Map<Integer, BigDecimal> priceOverride = new HashMap<>();
        Map<Integer, Integer> availOverride = new HashMap<>();
        if (dto.getFlightSeatInventoryDTOS() != null) {
            for (FlightSeatInventoryDTO it : dto.getFlightSeatInventoryDTOS()) {
                if (it.getSeatTypeId() == null) continue;
                if (it.getPrice() != null) priceOverride.put(it.getSeatTypeId(), it.getPrice());
                if (it.getAvailable() != null) availOverride.put(it.getSeatTypeId(), it.getAvailable());
            }
        }

        // 3) 若 planeId 变更：重建库存 = 用新 plane 模板的 capacity 作为 total/available
        if (planeChanged) {
            // 删除旧库存
            adminFlightMapper.deleteInventoryByFlightId(flightId);

            // 读取新飞机模板
            List<PlaneSeatCapacity> caps = adminPlaneMapper.getPlaneSeatCapacityByPlaneId(update.getPlaneId());
            if (caps == null || caps.isEmpty()) {
                throw new RuntimeException("该飞机未配置舱位容量模板");
            }

            // 构造库存行：total=capacity, available=capacity，然后应用覆盖
            List<FlightSeatInventory> rows = new ArrayList<>(caps.size());
            for (PlaneSeatCapacity c : caps) {
                int total = Math.max(0, c.getCapacity());
                int available = total;

                // 覆盖 available（限制在 0..total）
                if (availOverride.containsKey(c.getSeatTypeId())) {
                    int want = availOverride.get(c.getSeatTypeId());
                    available = Math.min(Math.max(0, want), total);
                }

                BigDecimal price = priceOverride.getOrDefault(c.getSeatTypeId(), BigDecimal.ZERO);

                FlightSeatInventory inv = new FlightSeatInventory();
                inv.setFlightId(flightId);
                inv.setSeatTypeId(c.getSeatTypeId());
                inv.setTotal(total);
                inv.setAvailable(available);
                inv.setPrice(price);
                rows.add(inv);
            }
            adminFlightMapper.batchInsertInventory(rows);
        } else {
            // 4) planeId 未变更：仅对传入的舱位逐条更新 price/available（保持 total 不变）
            if (!priceOverride.isEmpty() || !availOverride.isEmpty()) {
                for (Integer seatTypeId : unionKeys(priceOverride.keySet(), availOverride.keySet())) {
                    BigDecimal price = priceOverride.getOrDefault(seatTypeId, null);
                    Integer available = availOverride.getOrDefault(seatTypeId, null);

                    // 如果需要防止 available > total，可在 SQL 端用 LEAST，也可先查一遍 total 做裁剪
                    // 这里走 SQL 端保障（见 XML）
                    adminFlightMapper.updateInventoryOne(flightId, seatTypeId, available, price);
                }
            }
        }

        // 5) 组装返回 VO
        Plane plane = adminPlaneMapper.getById(update.getPlaneId());
        List<FlightSeatVO> seatVOS = adminFlightMapper.listSeatViewByFlightId(flightId);

        return getFlightVO(update, seatVOS, plane);
    }

    private static <T> Set<T> unionKeys(Set<T> a, Set<T> b) {
        Set<T> s = new HashSet<>();
        if (a != null) s.addAll(a);
        if (b != null) s.addAll(b);
        return s;
    }


    public PageResult<FlightVO> getAllAirlines(Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : size;
        int offset = (p - 1) * s;

        long total = adminFlightMapper.countAllFlights();
        List<FlightVO> records = total == 0
                ? Collections.emptyList()
                : adminFlightMapper.pageFlightList(offset, s);

        // 返回时带上 page/size
        log.info("查询所有航班分页: total={}, page={}, size={},flight={}", total, p, s, records);
        return new PageResult<>(total, records, p, s);
    }

    public FlightVO getFlightById(Integer id) {
        if (id == null) throw new IllegalArgumentException("id 不能为空");

        // 1) 头部信息（航班 + 飞机名 + 出发/到达机场名）
        FlightVO vo = adminFlightMapper.getFlightViewById(id);
        if (vo == null) {
            throw new RuntimeException("航班不存在");
        }

        // 2) 座位明细（舱位名称 + 价格 + 可售 + 总量）
        List<FlightSeatVO> seats = adminFlightMapper.listSeatViewByFlightId(id);
        vo.setFlightSeatVOS(seats);

        return vo;
    }
}
