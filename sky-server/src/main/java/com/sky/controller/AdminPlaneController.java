package com.sky.controller;

import com.sky.dto.PlaneDTO;
import com.sky.entity.Plane;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AdminPlaneService;
import com.sky.vo.PlaneVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Slf4j
@Api(tags = "管理员飞机相关接口")
public class AdminPlaneController {

    @Autowired
    private AdminPlaneService adminPlaneService;

    // 新增
    @PostMapping("/plane/add")
    public Result<Plane> addPlane(@RequestBody PlaneDTO planeDTO){
        log.info("新增飞机: {}", planeDTO);
        Plane res = adminPlaneService.addPlane(planeDTO);
        return Result.success(res);
    }

    // 删除
    @GetMapping("/plane/delete/{id}")
    public Result<Integer> deletePlane(@PathVariable Integer id){
        log.info("删除飞机: {}", id);
        adminPlaneService.deletePlane(id);
        return Result.success(id);
    }

    // 修改
    @PostMapping("/plane/update")
    public Result<Plane> updatePlane(@RequestBody PlaneDTO planeDTO) {
        log.info("修改飞机: {}", planeDTO);
        Plane res = adminPlaneService.updatePlane(planeDTO);
        return Result.success(res);
    }

    // 查询一个
    @GetMapping("/plane/{id}")
    public Result<Plane> getPlane(@PathVariable Integer id){
        log.info("查询飞机: {}", id);
        Plane p = adminPlaneService.getPlane(id);
        return Result.success(p);
    }

    // 查询全部（分页）
    @GetMapping("/plane/list")
    public Result<PageResult<Plane>> getAllPlanes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("查询所有飞机 page={}, size={}", page, size);
        PageResult<Plane> pr = adminPlaneService.getAllPlanes(page, size);
        return Result.success(pr);
    }

    /**
     * 可选条件 + 分页查询（这里给出 name 的可选条件；若你未来扩展更多字段可继续加）
     * 你的示例里有 city/address，但 Plane 只有 id/name，这里只保留 name。
     */
    @GetMapping("/planes")
    public Result<PageResult<Plane>> searchPlanes(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResult<Plane> pr = adminPlaneService.searchPlanes(name, page, size);
        return Result.success(pr);
    }
    @GetMapping("/planes/detail/{id}")
    public Result<PlaneVO> getPlaneDetail(@PathVariable Integer id) {
        log.info("查询飞机详情: {}", id);
        PlaneVO planeVO = adminPlaneService.getPlaneDetail(id);
        return Result.success(planeVO);
    }

}
