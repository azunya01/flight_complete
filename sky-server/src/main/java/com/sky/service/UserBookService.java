package com.sky.service;

import com.sky.dto.BookDTO;
import com.sky.dto.PassengerDTO;
import com.sky.mapper.UserBookMapper;
import com.sky.vo.BookVO;
import com.sky.vo.PassengerVO;
import com.sky.vo.UserFlightDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserBookService {
    @Autowired
    private UserBookMapper userBookMapper;

    public UserFlightDetailVO detail(Long flightId) {
        return userBookMapper.getFlightDetailById(flightId);
    }

    @Transactional
    public BookVO book(BookDTO bookDTO) {
        Objects.requireNonNull(bookDTO, "bookDTO is null");
        Integer flightId = Objects.requireNonNull(bookDTO.getFlightId(), "flightId is null");
        String userIdStr = bookDTO.getUserId() == null ? null : String.valueOf(bookDTO.getUserId());

        List<PassengerDTO> pax = Objects.requireNonNull(bookDTO.getPassengers(), "passengers is null");
        if (pax.isEmpty()) throw new IllegalArgumentException("乘客不能为空");

        // ===== 0) 先确保每个乘客都有 passengerId：用 userId + idNo 查/建 =====
        for (PassengerDTO p : pax) {
            if (p.getPassengerId() != null) continue; // 已有ID则跳过

            if (userIdStr == null || p.getIdNo() == null || p.getIdNo().isBlank()) {
                throw new IllegalArgumentException("缺少 userId 或乘客证件号 idNo");
            }
            Long pid = userBookMapper.findPassengerIdByUserAndIdNo(userIdStr, p.getIdNo());
            if (pid == null) {
                // 自动创建一条常用乘客记录（用传入的姓名/性别/电话）
                userBookMapper.insertPassengerFromSnapshot(
                        userIdStr, p.getName(), p.getGender(), p.getIdNo(), p.getPhone());
                pid = userBookMapper.lastInsertId();
            }
            p.setPassengerId(pid.intValue());
        }

        // ===== 1) 航班名 =====
        String flightName = userBookMapper.getFlightNameById(flightId);
        if (flightName == null) throw new IllegalArgumentException("航班不存在: id=" + flightId);

        int count = pax.size();

        // ===== 2) 选舱 & 扣减库存（与你现有一致） =====
        Integer seatTypeId = bookDTO.getSeatTypeId();
        BigDecimal unitPrice;
        if (seatTypeId != null) {
            unitPrice = userBookMapper.getSeatTypePrice(flightId, seatTypeId);
            log.info("User selected seatTypeId={}, unitPrice={}", seatTypeId, unitPrice);
            if (unitPrice == null) throw new IllegalArgumentException("该航班不售此舱位: seatTypeId=" + seatTypeId);
            int ok = userBookMapper.decreaseAvailable(flightId, seatTypeId, count);
            if (ok == 0) throw new RuntimeException("余票不足，请更换舱位或重试");
        } else {
            var options = userBookMapper.listSeatOptions(flightId);
            var chosen = options.stream()
                    .filter(op -> op.getAvailable() != null && op.getAvailable() >= count)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("余票不足，无法一次性购买 " + count + " 张"));
            seatTypeId = chosen.getSeatTypeId();
            unitPrice = chosen.getPrice();
            int ok = userBookMapper.decreaseAvailable(flightId, seatTypeId, count);
            if (ok == 0) throw new RuntimeException("余票不足，请重试");
        }

        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(count));

        // ===== 3) 订单头 =====
        userBookMapper.insertOrder(userIdStr, flightId, 1, total);
        Long orderId = userBookMapper.lastInsertId();
        if (orderId == null) throw new RuntimeException("创建订单失败");

        // ===== 4) 明细（此时 pax 内已全部带有 passengerId） =====
        userBookMapper.insertOrderItems(orderId, flightId, seatTypeId, unitPrice, pax);

        // ===== 5) 回显乘客信息（从 passenger 表查） =====
        var passengerIds = pax.stream().map(PassengerDTO::getPassengerId).toList();
        var passengerVOs = userBookMapper.getPassengersByIds(passengerIds);

        BookVO vo = new BookVO();
        vo.setOrderId(String.valueOf(orderId));
        vo.setFlightName(flightName);
        vo.setTotalPrice(total);
        vo.setCreateTime(LocalDateTime.now());
        vo.setPassengers(passengerVOs);
        return vo;
    }
}
