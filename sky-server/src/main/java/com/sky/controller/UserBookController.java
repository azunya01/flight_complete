package com.sky.controller;


import com.sky.dto.BookDTO;
import com.sky.result.Result;
import com.sky.service.UserBookService;
import com.sky.utils.UserContext;
import com.sky.vo.BookVO;
import com.sky.vo.UserFlightDetailVO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/book")
@Slf4j
@RequiredArgsConstructor
@Api(tags = "预定用户航班相关接口")
public class UserBookController {

    @Autowired
    private UserBookService userBookService;
    @GetMapping
    public Result<UserFlightDetailVO> detail(@RequestParam("flightId") Long flightId) {
        log.info("Get flight detail: {}", flightId);
        UserFlightDetailVO flightDetailVO = userBookService.detail(flightId);
        log.info("Get book detail: {}", flightDetailVO);
        return Result.success(flightDetailVO);
    }

    @PostMapping
    public Result<BookVO> book(@RequestBody BookDTO bookDTO) {
        Integer userId = Math.toIntExact(UserContext.getUserId());
        bookDTO.setUserId(userId);
        log.info("book: {}", bookDTO);
        BookVO bookVO=userBookService.book(bookDTO);
        return Result.success(bookVO);
    }
}
