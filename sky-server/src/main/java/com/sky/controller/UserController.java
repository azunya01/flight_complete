package com.sky.controller;
import com.sky.constant.JwtClaimsConstant;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;

import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user") // 方案A 用 /user；若走方案B，改成 /api 或 /api/user
@Slf4j
@Api(tags="用户登录相关接口")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    // 注册
    @PostMapping("/register")
    @ApiOperation(value="用户注册")
    public Result<UserVO> register(@RequestBody User user) {
        log.info("用户注册：{}", user);
        UserVO u = userService.register(user);
        return buildAuthResult(u);  // ★ 不再传 user.getId()
    }

    // 登录
    @PostMapping("/login")
    @ApiOperation(value="用户登录")
    public Result<UserVO> login(@RequestBody User user) {
        log.info("用户登录：{}", user);
        UserVO u = userService.login(user);
        return buildAuthResult(u);  // ★ 用服务层返回的 u.getId()
    }

    /** 生成 token 并回填到 UserVO */
    private Result<UserVO> buildAuthResult(UserVO u) {
        // 这里一定要用 u.getId()（数据库里的真实ID）
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, u.getId());


        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        u.setToken(token);
        return Result.success(u);
    }
}
