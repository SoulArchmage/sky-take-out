package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

/**
 * ClassName: DishService
 * Package: com.sky.service
 * Description:
 *
 * @Author pkq
 * @Create 2024-04-04 13:58
 * @Version 1.0
 */
public interface DishService  {
    void addDish(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);
}

