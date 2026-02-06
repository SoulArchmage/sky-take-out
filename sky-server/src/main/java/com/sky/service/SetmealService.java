package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface SetmealService {

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQuery);

    void add(SetmealDTO setmealDTO);

    void delete(List<Long> ids);
}
