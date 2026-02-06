package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQuery) {
        PageHelper.startPage(setmealPageQuery.getPage(), setmealPageQuery.getPageSize());
        Page<Setmeal> p = setmealMapper.pageQuery(setmealPageQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }
}
