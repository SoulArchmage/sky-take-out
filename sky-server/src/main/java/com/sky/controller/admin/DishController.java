package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.mapper.DishMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: DishController
 * Package: com.sky.controller.admin
 * Description: 新增菜品
 *
 * @Author pkq
 * @Create 2024-04-04 13:54
 * @Version 1.0
 */
@RestController
@Api(tags = "菜品相关接口")
@Slf4j
@CrossOrigin
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);
        dishService.addDish(dishDTO);
        return Result.success();
    }
}

